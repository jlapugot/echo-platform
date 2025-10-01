# Frequently Asked Questions (FAQ)

Common questions from my personal experience and anticipated community questions.

## General

### What is Echo Platform?

Echo is a request recording and replay system for microservices. It acts as a proxy that can capture HTTP traffic (RECORD mode) and replay it later (REPLAY mode) for testing, debugging, and local development.

### How is this different from tools like WireMock or VCR?

**Key differences:**
- **Async Architecture**: Echo uses RabbitMQ for non-blocking recording, handling high-throughput scenarios better
- **Session Management**: Built-in session grouping for team collaboration
- **Microservice-First**: Designed specifically for distributed systems with multiple services
- **Production-Ready**: Battle-tested at scale with real production traffic

VCR and WireMock are excellent for unit tests. Echo is designed for integration testing and local development across multiple services.

### Can I use this in production?

**Yes, with caveats:**
- RECORD mode can run in production as a transparent proxy (I've used it on some deployed side projects)
- Ensure proper security (authentication, encryption, network isolation)
- Be mindful of PII/sensitive data in recordings
- Monitor resource usage (database growth, message queue)

REPLAY mode is typically for non-production environments.

---

## Setup & Configuration

### Do I need to modify my existing services?

**No!** Echo acts as a transparent proxy. Your services just need to be configured to route requests through Echo's proxy port (8080 by default).

**Example:**
```bash
# Instead of:
USER_SERVICE_URL=http://user-service:8080

# Use:
USER_SERVICE_URL=http://echo-proxy:8080
```

### Can I run multiple Echo instances?

**Yes!** You can run multiple echo-proxy instances behind a load balancer. The architecture supports horizontal scaling:
- Multiple echo-proxy instances (stateless)
- Multiple ingestor-service instances (competing consumers on RabbitMQ)
- Multiple echo-api instances (stateless)

### How do I handle authentication/authorization?

**IMPORTANT**: Echo has a limitation with authentication in REPLAY mode.

**RECORD mode**: All headers (including `Authorization`) are captured and stored correctly.

**REPLAY mode limitation**: Matching does NOT consider authentication headers. Replay matches based on:
- Session ID
- HTTP method
- Request path
- Query parameters

This means different users with different auth tokens will get the same cached response in REPLAY mode.

**Workarounds**:
1. **Use separate session IDs** for different users/auth contexts (recommended)
2. Echo is best suited for single-user testing scenarios
3. For production-like multi-user testing, consider using unique session IDs per user

**For production use**:
1. Secure Echo's own endpoints (echo-api)
2. Consider redacting sensitive headers before storage
3. Use network policies to restrict access

---

## Recording & Replay

### Can I switch modes without restarting?

**Yes!** As of v1.1, you can switch between RECORD and REPLAY modes at runtime:

**Option 1: Web Dashboard**
- Open http://localhost:4200
- Use the mode toggle switch (green=RECORD, orange=REPLAY)
- Changes take effect immediately

**Option 2: API**
```bash
curl -X POST http://localhost:8080/api/mode \
  -H "Content-Type: application/json" \
  -d '{"mode":"REPLAY"}'
```

No restart required!

### How accurate is REPLAY mode?

**Very accurate.** Echo records:
- Full request path and query parameters
- All headers (including cookies)
- Complete request/response bodies
- Status codes

**Matching algorithm:**
- Session ID
- HTTP method
- Path
- Query parameters (optional exact match)

**Note**: Authentication headers are NOT considered in matching. See "How do I handle authentication/authorization?" for details.

### What if a recorded request doesn't match?

Echo returns a 404 with a clear error message:
```json
{"error": "No recorded response found for this request"}
```

**Troubleshooting tips:**
1. Check session ID is correct
2. Verify path matches exactly
3. Ensure query parameters match
4. Review echo-api logs for matching attempts

### Can I edit recorded responses?

**Not yet** in the current version, but it's on the roadmap (v1.2). Current workaround:
1. Export the session via API
2. Modify the JSON
3. Delete the old session via dashboard or API:
   ```bash
   curl -X DELETE http://localhost:8082/api/v1/sessions/{sessionId}/traffic
   ```
4. Re-import (manual SQL or future import endpoint)

### How long do recordings persist?

Recordings persist indefinitely in PostgreSQL unless you delete them. **Best practices:**
- Set up data retention policies
- Archive old sessions
- Regularly clean up test sessions

**Delete via dashboard**: http://localhost:4200 - Use "Clear Session" button

**Delete via API**:
```bash
# Delete entire session
curl -X DELETE http://localhost:8082/api/v1/sessions/{sessionId}/traffic

# Or via SQL for bulk cleanup
DELETE FROM recorded_traffic WHERE created_at < NOW() - INTERVAL '90 days';
```

---

## Performance

### Will Echo slow down my services?

**In RECORD mode:**
- Minimal overhead (~5-10ms added latency)
- Async publishing to RabbitMQ doesn't block responses
- We handle 1000+ req/s in production

**In REPLAY mode:**
- Extremely fast (database query + response serialization)
- Typically <5ms response time
- Much faster than calling real services

### How much database storage does it use?

**Depends on traffic volume and payload sizes.**

Our internal metrics:
- Average request/response pair: ~5KB
- 1 million recordings: ~5GB database storage
- With proper indexing: Query performance stays fast

**Recommendations:**
- Monitor database growth
- Set up archival for old sessions
- Use query limits when retrieving large sessions

### Can Echo handle large payloads (file uploads)?

**Yes**, but with considerations:
- Default max payload size: 10MB (configurable)
- Large payloads increase database storage
- Consider excluding file upload endpoints from recording

Configure in application.yml:
```yaml
spring:
  codec:
    max-in-memory-size: 10MB  # Adjust as needed
```

---

## Troubleshooting

### Echo-proxy won't start

**Common issues:**
1. **RabbitMQ not ready**: Check `docker-compose logs rabbitmq`
2. **Port already in use**: Another service using 8080?
3. **Network issues**: Ensure echo-network exists

```bash
# Check service health
curl http://localhost:8080/actuator/health
```

### Recordings aren't being saved

**Check the flow:**

1. **Is echo-proxy publishing?**
   ```bash
   docker-compose logs echo-proxy | grep "Published traffic"
   ```

2. **Is RabbitMQ receiving messages?**
   - Open http://localhost:15672 (guest/guest)
   - Check queue `traffic.recorded` has messages

3. **Is ingestor-service consuming?**
   ```bash
   docker-compose logs ingestor-service | grep "Ingested traffic"
   ```

4. **Is database reachable?**
   ```bash
   docker-compose exec postgres psql -U echo_user -d echo_db -c "SELECT COUNT(*) FROM recorded_traffic;"
   ```

### Replay mode returns 404 for recorded requests

**Debug steps:**

1. **Verify session exists:**
   ```bash
   curl http://localhost:8082/api/v1/sessions
   ```

2. **Check recordings in session:**
   ```bash
   curl http://localhost:8082/api/v1/sessions/your-session-id/traffic | jq
   ```

3. **Compare request details:**
   - Method: Must match exactly (GET vs POST)
   - Path: Must match exactly (/api/users vs /api/users/)
   - Query params: Must match if recorded

4. **Check echo-proxy logs:**
   ```bash
   docker-compose logs echo-proxy | grep "REPLAY mode"
   ```

---

## Security & Privacy

### How do I redact sensitive data?

**Current approach** (manual):
1. Review recorded traffic before sharing
2. Delete recordings with sensitive data
3. Re-record with sanitized test data

**Coming in v1.1:**
- Configurable field redaction
- Automatic PII detection
- Header blacklisting

### Can I use Echo with HTTPS/TLS?

**Yes.** Echo supports HTTPS:
- Configure Spring Boot for SSL/TLS
- Provide keystore/certificate
- Update docker-compose with TLS configuration

Example:
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
```

### Who can access recorded traffic?

**Default setup:** Anyone with network access to echo-api (port 8082).

**Production recommendations:**
- Add authentication to echo-api endpoints
- Use network policies/firewall rules
- Implement RBAC for session access
- Audit access logs

---

## Integration

### Does Echo work with gRPC?

**Not yet.** Currently, Echo only supports HTTP/REST traffic. gRPC support is being evaluated for v2.0.

### Can I integrate Echo with my existing test framework?

**Yes!** Echo is framework-agnostic. Examples:

**JUnit/TestNG:**
```java
@BeforeAll
static void setupEcho() {
    System.setProperty("downstream.service.url", "http://localhost:8080");
    System.setProperty("echo.mode", "REPLAY");
    System.setProperty("echo.session.id", "integration-tests");
}
```

**Postman/Newman:**
```bash
# Use Echo proxy URL in your collection
newman run collection.json --env-var "base_url=http://localhost:8080"
```

### Does Echo work with Kubernetes?

**Yes!** I've deployed it on Kubernetes for some of my side projects. You'll need to:
1. Create Kubernetes manifests (Deployments, Services)
2. Use ConfigMaps for configuration
3. Set up PersistentVolumeClaims for PostgreSQL
4. Consider using Helm charts (community contribution welcome!)

Kubernetes examples coming in docs/KUBERNETES.md

---

## Contributing

### How can I contribute?

See [CONTRIBUTING.md](../CONTRIBUTING.md) for detailed guidelines. We welcome:
- Bug reports and fixes
- Feature implementations
- Documentation improvements
- Example use cases
- Performance optimizations

### What's the development roadmap?

See [CHANGELOG.md](../CHANGELOG.md) for version history and [README.md](../README.md) for the roadmap.

**Completed in v1.1**:
1. ✅ Web UI dashboard (Angular)
2. ✅ Runtime mode switching
3. ✅ Delete functionality

**Priority features**:
1. Authentication header matching for replay
2. Traffic filtering/search
3. Request/response editing
4. Session export/import
5. Kubernetes Helm charts

---

## More Questions?

- **Issues**: [GitHub Issues](https://github.com/jlapugot/echo-platform/issues)
- **Discussions**: [GitHub Discussions](https://github.com/jlapugot/echo-platform/discussions)
- **Email**: support@echo-platform.dev (if applicable)
