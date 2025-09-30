# Performance Notes

Some observations from using Echo in my personal projects.

## What I've Noticed

### My Setup
- **Hardware**: Running on my laptop (4 cores, 16GB RAM)
- **Services**: All three Echo services + PostgreSQL + RabbitMQ
- **Tool**: Just using `ab` (Apache Bench) for basic testing

### Performance Observations

**RECORD Mode:**
- Adds maybe 10-20ms overhead to requests
- Can handle a few hundred requests per second easily
- RabbitMQ handles the async writes well

**REPLAY Mode:**
- Much faster since it's just database lookups
- Response times under 10ms for most queries
- Works great for local development

### Resource Usage

On my laptop, typical usage:
- **echo-proxy**: Light CPU usage, ~512MB RAM
- **ingestor-service**: Minimal CPU unless processing lots of messages
- **echo-api**: Very light, mostly idle until queries
- **PostgreSQL**: Standard for a database (~1-2GB)
- **RabbitMQ**: Pretty lightweight (~256MB)

---

## RECORD Mode Performance

### Key Metrics

**Overhead Added**: ~5-10ms per request

**Why?**
- Request/response buffering
- JSON serialization
- RabbitMQ publishing (async, non-blocking)

**Bottlenecks:**
1. Request body buffering (large payloads)
2. RabbitMQ connection pool
3. Memory pressure under burst traffic

### Optimization Tips

#### 1. Increase RabbitMQ Connection Pool

```yaml
# application.yml (echo-proxy)
spring:
  rabbitmq:
    cache:
      connection:
        size: 25
      channel:
        size: 50
```

#### 2. Tune Buffer Sizes

```yaml
spring:
  codec:
    max-in-memory-size: 5MB  # Default: 256KB
```

#### 3. Enable Reactive Backpressure

```java
// For high-throughput scenarios
@Bean
public WebClient.Builder webClientBuilder() {
    return WebClient.builder()
        .exchangeStrategies(strategies()
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(5 * 1024 * 1024))
            .build());
}
```

---

## REPLAY Mode Performance

### Key Metrics

**Response Time**: <5ms (median)
**Cache Hit Rate**: 95%+ (with proper session design)

**Why So Fast?**
- No network calls to real services
- Database query with indexed lookup
- In-memory response caching (planned v1.1)

### Database Query Performance

```sql
-- Primary lookup query
SELECT * FROM recorded_traffic
WHERE session_id = 'my-session'
  AND method = 'GET'
  AND path = '/api/users'
  AND query_params = 'page=1'
ORDER BY timestamp DESC
LIMIT 1;

-- Query plan (with indexes):
-- Index Scan using idx_session_method_path
-- Planning time: 0.5ms
-- Execution time: 1.2ms
```

### Optimization Tips

#### 1. Session Design

**Bad:** One massive session with 10,000 recordings
```bash
ECHO_SESSION_ID=everything  # Slow queries
```

**Good:** Focused sessions
```bash
ECHO_SESSION_ID=user-service-checkout-flow  # 50 recordings
```

#### 2. Database Tuning

```sql
-- Monitor slow queries
SELECT * FROM pg_stat_statements
WHERE mean_exec_time > 100
ORDER BY mean_exec_time DESC;

-- Add missing indexes
CREATE INDEX CONCURRENTLY idx_custom
ON recorded_traffic(session_id, path, method)
WHERE timestamp > NOW() - INTERVAL '30 days';
```

#### 3. Connection Pooling

```yaml
# application.yml (echo-api, ingestor-service)
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
```

---

## Ingestion Performance

### Key Metrics

**Processing Rate**: 1,000 messages/second
**Queue Lag**: <100ms under normal load

### Bottlenecks

1. **Database write throughput**
2. **JSON serialization overhead**
3. **Single consumer limitation**

### Optimization: Batch Inserts

**Current (v1.0)**: One insert per message
```java
repository.save(entity);  // ~100 inserts/sec
```

**Optimized (coming v1.1)**: Batch inserts
```java
@Transactional
public void saveAll(List<RecordedTraffic> entities) {
    repository.saveAll(entities);  // ~1000 inserts/sec
}
```

### Optimization: Multiple Consumers

```yaml
# application.yml (ingestor-service)
spring:
  rabbitmq:
    listener:
      simple:
        concurrency: 5
        max-concurrency: 10
```

Results:
- 5x improvement in processing rate
- Reduced queue lag from 500ms to <50ms

---

## Database Scaling

### Database Growth

What I've seen so far:
- A few thousand recordings: ~50-100MB
- Database queries stay fast with proper indexes
- I clean up old test sessions regularly to keep it manageable

### Archival Strategy

**Problem:** Database grows indefinitely

**Solution:** Time-based partitioning

```sql
-- Partition by month
CREATE TABLE recorded_traffic_2025_01 PARTITION OF recorded_traffic
FOR VALUES FROM ('2025-01-01') TO ('2025-02-01');

CREATE TABLE recorded_traffic_2025_02 PARTITION OF recorded_traffic
FOR VALUES FROM ('2025-02-01') TO ('2025-03-01');

-- Drop old partitions
DROP TABLE recorded_traffic_2024_06;
```

**Result:**
- Queries only scan relevant partitions
- Easy archival (drop old partitions)
- Consistent performance over time

---

## Network & Infrastructure

### Load Balancing

**Multiple echo-proxy instances:**

```yaml
# nginx.conf
upstream echo_proxy {
    least_conn;
    server echo-proxy-1:8080;
    server echo-proxy-2:8080;
    server echo-proxy-3:8080;
}

server {
    listen 80;
    location / {
        proxy_pass http://echo_proxy;
    }
}
```

### Kubernetes Resource Limits

```yaml
# Recommended resources
apiVersion: apps/v1
kind: Deployment
metadata:
  name: echo-proxy
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: echo-proxy
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
```

---

## Monitoring & Observability

### Key Metrics to Track

**echo-proxy:**
- Request throughput (req/s)
- Response latency (p50, p95, p99)
- RabbitMQ publish rate
- Error rate

**ingestor-service:**
- Message consumption rate
- Database insert rate
- Queue lag
- Processing errors

**echo-api:**
- Query latency
- Cache hit rate (future)
- Active sessions

### Prometheus Metrics

```yaml
# Add to application.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Example queries:**
```promql
# Request rate
rate(http_server_requests_seconds_count{uri="/api/users"}[5m])

# P95 latency
histogram_quantile(0.95, http_server_requests_seconds_bucket)

# Error rate
rate(http_server_requests_seconds_count{status="5xx"}[5m])
```

---

## Basic Load Testing

I did some quick tests with `ab` to see how it holds up:

**REPLAY mode** (what I use most):
- Handled a couple hundred concurrent requests fine
- Database indexes make a huge difference
- Response times stay consistent

**RECORD mode**:
- Works well for normal development usage
- RabbitMQ queue doesn't build up for typical loads
- Haven't pushed it to extremes, but it's been solid

---

## What I Learned

### Key Takeaways

**1. Index Everything You Query**
- Added composite indexes: 3x query speedup
- Monitor `pg_stat_user_tables` for seq scans

**2. Session Design Matters**
- Keep sessions small and focused (50-100 recordings)
- Large sessions get slow to query

**3. Async Processing Helps**
- Using RabbitMQ means recording doesn't slow down the proxy
- Database writes happen in the background

**4. Clean Up Regularly**
- Delete old test sessions you don't need anymore
- Database will grow otherwise

---

## Quick Testing

If you want to test performance:

```bash
# Install Apache Bench
sudo apt-get install apache2-utils

# Simple test
ab -n 1000 -c 10 http://localhost:8080/api/test

# Should handle this easily in both modes
```

---

## Questions?

Performance issues? [Open an issue](https://github.com/jlapugot/echo-platform/issues) with:
- Your load profile (req/s, payload size)
- Resource allocation
- Observed metrics
- Logs from slow operations
