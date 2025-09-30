# Echo Platform - Architecture Documentation

## Overview

Echo Platform is designed as a cloud-native microservices architecture following best practices for scalability, maintainability, and operational excellence.

## Core Principles

1. **Separation of Concerns**: Each service has a single, well-defined responsibility
2. **Asynchronous Communication**: High-performance operations decoupled via messaging
3. **Eventual Consistency**: Traffic recording doesn't block proxy operations
4. **Observability**: Health checks and logging for operational visibility
5. **Containerization**: Platform-agnostic deployment via Docker

## Service Breakdown

### 1. echo-proxy (Gateway Layer)

**Technology**: Spring Cloud Gateway (Reactive)

**Key Responsibilities**:
- Route management and traffic forwarding
- Request/response capture in RECORD mode
- Response replay in REPLAY mode
- Publish traffic events to message queue

**Design Decisions**:
- Uses reactive programming model for high throughput
- Global filters for cross-cutting concerns (recording, replaying)
- WebClient for non-blocking HTTP calls to echo-api
- RabbitMQ for fire-and-forget message publishing

**Flow - RECORD Mode**:
```
Client Request → RecordModeFilter (capture) → Route to Real Service
                                            ↓
                     Response ← Real Service
                                            ↓
              Publish to RabbitMQ ← Build TrafficRecord
                                            ↓
                        Return Response to Client
```

**Flow - REPLAY Mode**:
```
Client Request → ReplayModeFilter → Query echo-api for match
                                                    ↓
                           Return Recorded Response (or 404)
```

### 2. ingestor-service (Data Layer)

**Technology**: Spring Boot + Spring Data JPA + RabbitMQ Consumer

**Key Responsibilities**:
- Consume traffic events from message queue
- Transform and persist to PostgreSQL
- Handle database schema migrations via Liquibase

**Design Decisions**:
- RabbitMQ listener with JSON deserialization
- Transactional persistence for data integrity
- JSON serialization for header storage (flexibility)
- Indexed columns for query performance

**Data Flow**:
```
RabbitMQ Queue → TrafficListener → TrafficIngestionService
                                                    ↓
                            RecordedTrafficRepository
                                                    ↓
                                    PostgreSQL (via Hibernate)
```

### 3. echo-api (Query/API Layer)

**Technology**: Spring Boot + Spring Web + Spring Data JPA

**Key Responsibilities**:
- Provide REST APIs for traffic retrieval
- Support session-based querying
- Internal endpoint for replay matching
- Future: Dashboard UI backend

**Design Decisions**:
- Read-only operations on database
- DTO pattern for clean API contracts
- Custom JPQL queries for complex matching logic
- JSON deserialization for header retrieval

**API Patterns**:
- Public endpoints: `/api/v1/sessions/*`
- Internal endpoints: `/api/v1/internal/*`
- RESTful resource naming
- Standard HTTP status codes

## Data Model

### RecordedTraffic Entity

```sql
CREATE TABLE recorded_traffic (
    id BIGSERIAL PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    path VARCHAR(2048) NOT NULL,
    query_params VARCHAR(2048),
    request_headers TEXT,
    request_body TEXT,
    status_code INTEGER NOT NULL,
    response_headers TEXT,
    response_body TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_session_id ON recorded_traffic(session_id);
CREATE INDEX idx_session_method_path ON recorded_traffic(session_id, method, path);
```

**Indexing Strategy**:
- `idx_session_id`: Fast session-based queries
- `idx_session_method_path`: Optimized replay matching

## Message Flow

### RabbitMQ Configuration

**Queue**: `traffic.recorded`
**Exchange**: Default (direct)
**Message Format**: JSON
**Delivery**: Persistent messages (survives broker restart)

**Message Schema** (TrafficRecord):
```json
{
  "sessionId": "string",
  "method": "string",
  "path": "string",
  "queryParams": "string",
  "requestHeaders": {"key": "value"},
  "requestBody": "string",
  "statusCode": 200,
  "responseHeaders": {"key": "value"},
  "responseBody": "string",
  "timestamp": "2025-09-30T10:15:30Z"
}
```

## Scalability Considerations

### Current Design (MVP)

- Single instance of each service
- Single PostgreSQL database
- Single RabbitMQ broker

### Future Scaling Options

1. **Horizontal Scaling**:
   - Multiple echo-proxy instances (load balanced)
   - Multiple ingestor-service instances (competing consumers)
   - Multiple echo-api instances (stateless, easily scalable)

2. **Database Optimization**:
   - Read replicas for echo-api
   - Partitioning by session_id or timestamp
   - Archival strategy for old sessions

3. **Message Queue**:
   - RabbitMQ clustering for high availability
   - Queue partitioning for throughput

4. **Caching Layer**:
   - Redis cache for frequently accessed recordings
   - CDN for static UI assets (future dashboard)

## Security Considerations

### Current Implementation (Development)

- No authentication/authorization
- Internal network communication
- Default credentials

### Production Recommendations

1. **API Security**:
   - OAuth 2.0 / JWT authentication
   - Rate limiting
   - API gateway with request validation

2. **Service-to-Service**:
   - mTLS for inter-service communication
   - Service mesh (Istio, Linkerd)

3. **Data Protection**:
   - Encryption at rest (database)
   - Encryption in transit (TLS)
   - PII masking/redaction for sensitive data

4. **Infrastructure**:
   - Network policies (Kubernetes)
   - Secret management (Vault, AWS Secrets Manager)
   - Regular security scanning

## Operational Considerations

### Health Checks

All services expose Spring Boot Actuator endpoints:
- `/actuator/health`: Service health status
- `/actuator/info`: Service information

Docker Compose includes health checks for:
- PostgreSQL: `pg_isready`
- RabbitMQ: `rabbitmq-diagnostics`
- Services: HTTP health endpoints

### Logging Strategy

- Structured logging (SLF4J + Logback)
- Log levels: INFO (default), DEBUG (development), ERROR
- Key log points:
  - Request/response capture
  - Message publishing/consumption
  - Database operations
  - Error conditions

### Monitoring Metrics (Future)

- Request throughput (echo-proxy)
- Message queue depth (RabbitMQ)
- Database query performance
- Error rates and latencies
- System resource utilization

## Testing Strategy

### Unit Tests
- Service layer business logic
- Message serialization/deserialization
- DTO conversions

### Integration Tests
- Controller endpoints (MockMvc)
- Repository queries (TestContainers)
- RabbitMQ message flow

### End-to-End Tests (Future)
- Full RECORD → REPLAY cycle
- Multi-service interaction
- Performance under load

## Technology Choices - Rationale

| Technology | Alternative Considered | Why Chosen |
|------------|----------------------|------------|
| Spring Cloud Gateway | Zuul, Kong | Native Spring integration, reactive |
| RabbitMQ | Kafka, SQS | Simpler setup, good for MVP |
| PostgreSQL | MongoDB, MySQL | ACID compliance, mature tooling |
| Liquibase | Flyway | XML-based changesets, rollback support |
| Docker Compose | Kubernetes | Simpler local development |
| Gradle | Maven | Better multi-project support |

## Future Enhancements

1. **Dashboard UI**: React/Angular frontend for traffic visualization
2. **Traffic Filtering**: Advanced query capabilities (date range, status code)
3. **Traffic Editing**: Modify recorded responses before replay
4. **Performance Testing**: Load testing mode with traffic amplification
5. **Export/Import**: Share recorded sessions between teams
6. **Webhook Support**: Notify external systems of recordings
7. **GraphQL API**: Alternative to REST for flexible queries

---

**Document Version**: 1.0.0
**Last Updated**: 2025-09-30
**Author**: Echo Platform Team