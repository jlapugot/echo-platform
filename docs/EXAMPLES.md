# Usage Examples

Here are some practical ways I've been using Echo in my personal projects.

## Table of Contents

1. [Local Development Without Dependencies](#1-local-development-without-dependencies)
2. [Integration Testing in CI/CD](#2-integration-testing-in-cicd)
3. [Debugging Production Issues Locally](#3-debugging-production-issues-locally)
4. [Team Collaboration with Shared Sessions](#4-team-collaboration-with-shared-sessions)
5. [Performance Testing Service in Isolation](#5-performance-testing-service-in-isolation)

---

## 1. Local Development Without Dependencies

### Scenario
I'm developing a new feature in my `user-service` that calls my `payment-service` and `notification-service`. Without Echo, I'd need to run all three services locally.

### Solution: Record Once, Develop Offline

**Step 1: Record traffic in staging environment**

```bash
# Start Echo in RECORD mode, pointing to staging
docker-compose up -d

# Configure echo-proxy
export ECHO_MODE=RECORD
export ECHO_SESSION_ID=user-service-dev
export ECHO_TARGET_URL=https://staging.api.example.com

# Route your user-service requests through Echo
curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "amount": 50.00}'

curl -X POST http://localhost:8080/api/notifications \
  -H "Content-Type: application/json" \
  -d '{"userId": 123, "message": "Payment successful"}'
```

**Step 2: Switch to REPLAY mode for development**

```bash
# Stop echo-proxy
docker-compose stop echo-proxy

# Update environment
export ECHO_MODE=REPLAY
export ECHO_SESSION_ID=user-service-dev

# Restart
docker-compose up -d echo-proxy
```

**Step 3: Develop offline**

Now my `user-service` can call Echo proxy and get realistic responses without needing any other services running!

```bash
# My service calls:
# http://localhost:8080/api/payments -> returns recorded response
# http://localhost:8080/api/notifications -> returns recorded response
```

**Result:** Reduced my setup time from ~15 minutes (starting multiple services) to under a minute (just Echo).

---

## 2. Integration Testing in CI/CD

### Scenario
My CI/CD pipeline for side projects needs to test service interactions, but spinning up multiple services is slow and expensive on free CI tiers.

### Solution: Record-Once, Test-Always Pattern

**Step 1: Create test recordings**

```bash
# In your test setup script
export ECHO_MODE=RECORD
export ECHO_SESSION_ID=ci-integration-tests

# Run your integration test suite once to record
npm run test:integration

# Save the recorded session
curl http://localhost:8082/api/v1/sessions/ci-integration-tests/traffic > test-recordings.json
```

**Step 2: Use in CI pipeline**

```yaml
# .gitlab-ci.yml or .github/workflows/test.yml
integration-tests:
  services:
    - postgres:15
    - rabbitmq:3.12

  before_script:
    - docker-compose up -d echo-api echo-proxy
    - echo "ECHO_MODE=REPLAY" >> .env
    - echo "ECHO_SESSION_ID=ci-integration-tests" >> .env

  script:
    - ./gradlew :user-service:integrationTest

  # Tests run against Echo in REPLAY mode
  # No need for actual payment-service or notification-service
```

**Result:** Tests run faster and more reliably since they don't depend on multiple services being up.

---

## 3. Debugging Production Issues Locally

### Scenario
I encounter a bug in one of my deployed side projects that only happens with specific API responses. I need to reproduce it locally.

### Solution: Record Production Request, Replay Locally

**Step 1: Capture production traffic** (with PII redaction)

```bash
# Temporarily route production request through Echo (in a safe environment)
# Or extract from production logs and replay through Echo

export ECHO_MODE=RECORD
export ECHO_SESSION_ID=bug-investigation-issue-456

# Send the problematic request
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer xxx" \
  -d @production-request.json
```

**Step 2: Reproduce bug locally**

```bash
export ECHO_MODE=REPLAY
export ECHO_SESSION_ID=bug-investigation-issue-456

# Run your service in debug mode
./gradlew :order-service:bootRun --debug-jvm

# The service will get the exact same responses from dependencies
# Now you can step through with a debugger
```

**Result:** Way easier to debug issues when I can replay the exact scenario that caused the problem.

---

## 4. Saving Test Scenarios for Different Features

### Scenario
I'm working on multiple features in parallel and need to quickly switch contexts between different test scenarios.

### Solution: Use Named Sessions

**Save scenario for Feature A:**

```bash
export ECHO_MODE=RECORD
export ECHO_SESSION_ID=feature-checkout-flow

# Execute test flow for checkout feature
./scripts/test-checkout.sh
```

**Save scenario for Feature B:**

```bash
export ECHO_MODE=RECORD
export ECHO_SESSION_ID=feature-user-profile

# Execute test flow for user profile feature
./scripts/test-profile.sh
```

**Switch between features instantly:**

```bash
# Working on checkout
export ECHO_MODE=REPLAY
export ECHO_SESSION_ID=feature-checkout-flow
./gradlew bootRun

# Switch to user profile
export ECHO_SESSION_ID=feature-user-profile
docker-compose restart echo-proxy
./gradlew bootRun
```

**Result:** I can context-switch between features in seconds. Each feature has its own isolated test data.

---

## 5. Performance Testing Service in Isolation

### Scenario
I want to performance test my `order-service` to find bottlenecks without being limited by downstream service performance.

### Solution: Replay Mode for Isolated Performance Testing

**Step 1: Record representative traffic**

```bash
export ECHO_MODE=RECORD
export ECHO_SESSION_ID=perf-test-orders

# Record various order scenarios
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/orders \
    -d @test-data/order-$i.json
done
```

**Step 2: Performance test in isolation**

```bash
export ECHO_MODE=REPLAY
export ECHO_SESSION_ID=perf-test-orders

# Run load test with tools like k6, JMeter, or Gatling
k6 run --vus 100 --duration 5m performance-test.js

# All downstream calls return instantly from Echo
# Pure measurement of order-service performance
```

**Metrics collected:**
- Service throughput: 1000 req/s (without downstream latency)
- Memory usage under load
- Database query performance
- CPU utilization

**Result:** Helped me identify that my service's database queries were the bottleneck, not external API calls.

---

## Tips & Tricks I've Picked Up

### Session Naming Convention

I use this pattern: `{project}-{feature}-{purpose}`

Examples:
- `ecommerce-checkout-flow-dev`
- `blog-api-auth-debug`
- `ci-integration-user-service`
- `local-payment-refund-testing`

### Session Lifecycle

1. **Record** from real services (local, staging, or production with safeguards)
2. **Test** the recording works in REPLAY mode
3. **Version control** (commit session exports to git for backup)
4. **Archive** old sessions quarterly

### Data Hygiene

```bash
# Always review recorded traffic for sensitive data
curl http://localhost:8082/api/v1/sessions/my-session/traffic | \
  jq '.[] | select(.requestBody | contains("password"))'

# Redact if found
# DELETE and re-record with sanitized data
```

### Performance Tips

- Keep sessions focused (10-100 requests, not thousands)
- Use session IDs that expire/rotate
- Archive old sessions to separate database
- Monitor Echo's resource usage

---

## Community Examples

Have a great use case? [Submit a PR](../CONTRIBUTING.md) to add it here!

### Example Template

```markdown
## Your Use Case Title

### Scenario
Brief description

### Solution
Step-by-step with code examples

### Result
What was achieved
```