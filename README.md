# Echo Platform 🎙️

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green.svg)](https://spring.io/projects/spring-boot)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](CONTRIBUTING.md)

> **A request recorder and replay service for microservices** - Think "VCR for API traffic"

**I built this tool for my personal side projects, and now I'm open-sourcing it!**

Echo Platform enables developers to record real HTTP traffic between microservices and replay it later for isolated testing, debugging, and development. I created it to solve a frustrating problem in my personal projects: running multiple services locally just to test one feature.

### Why I Built Echo (And Why I'm Open-Sourcing It)

I was working on a few side projects that used microservices (typically 3-5 services per project), and constantly having to start all of them just to develop or test one was painful. Maintaining mock data was tedious and often out of sync with reality.

**What I learned along the way:**
- Recording actual API responses is way better than maintaining mock JSON files
- Being able to save different "recording sessions" for different scenarios is incredibly useful
- It's much easier to debug when you can replay the exact same responses

**After using this across a few projects and refining it over time, I thought it might be useful to others, so here it is!**

## ✨ Why Echo?

**The Problem:** Modern microservice architectures make local development painful. To test Service A, you need Services B, C, and D running, each with their own databases, message queues, and configurations.

**The Solution:** Echo sits as a transparent proxy. In RECORD mode, it captures real request/response pairs. In REPLAY mode, it becomes a high-fidelity mock server, returning the exact responses that were previously recorded.

### Key Benefits

- 🚀 **Zero-dependency testing** - Test services in isolation
- 🎯 **High-fidelity mocks** - Real production data, not hand-crafted stubs
- ⚡ **Fast local development** - No need to spin up entire dependency chains
- 🔄 **Session management** - Group and reuse recordings by test scenario
- 📊 **Full transparency** - Inspect complete request/response pairs (headers, body, status)

## 🏗️ Architecture

Echo consists of three microservices in an event-driven architecture:

```
┌─────────────────┐
│   echo-proxy    │  ← Your app connects here (Port 8080)
│  (Record/Replay)│
└────────┬────────┘
         │ Publishes traffic
         ▼
┌─────────────────┐
│   RabbitMQ      │  ← Async message queue
└────────┬────────┘
         │ Consumes
         ▼
┌─────────────────┐      ┌─────────────────┐
│ ingestor-service│◄────►│   PostgreSQL    │
└─────────────────┘      └─────────────────┘
                                 ▲
                                 │ Queries
                         ┌───────┴────────┐
                         │   echo-api     │  ← REST API (Port 8082)
                         └────────────────┘
```

### Components

| Service | Technology | Responsibility |
|---------|-----------|----------------|
| **echo-proxy** | Spring Cloud Gateway | Routes traffic (RECORD) or returns mocks (REPLAY) |
| **ingestor-service** | Spring Boot + JPA | Consumes queue, persists to PostgreSQL |
| **echo-api** | Spring Boot REST | Query API for recorded traffic |

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose
- Java 17+ (for local development)
- Gradle 8.5+ (for local development)

### Run with Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/jlapugot/echo-recorder.git
cd echo-recorder

# Start everything
docker-compose up --build

# Verify services are healthy
curl http://localhost:8080/actuator/health  # echo-proxy
curl http://localhost:8081/actuator/health  # ingestor-service
curl http://localhost:8082/actuator/health  # echo-api
```

**Access Points:**
- Echo Proxy: `http://localhost:8080`
- Echo API: `http://localhost:8082`
- RabbitMQ Management: `http://localhost:15672` (guest/guest)

## 📖 Usage Guide

### Recording Traffic (RECORD Mode)

1. **Configure the proxy** to point to your real service:
   ```bash
   export ECHO_MODE=RECORD
   export ECHO_SESSION_ID=user-service-test
   export ECHO_TARGET_URL=http://your-real-service:9000
   ```

2. **Send requests through Echo:**
   ```bash
   # All requests are forwarded and recorded
   curl http://localhost:8080/api/users
   curl http://localhost:8080/api/users/123
   curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{"name": "Alice"}'
   ```

3. **View recorded traffic:**
   ```bash
   curl http://localhost:8082/api/v1/sessions/user-service-test/traffic | jq
   ```

### Replaying Traffic (REPLAY Mode)

1. **Switch to REPLAY mode:**
   ```bash
   # Update docker-compose.yml or set environment variable
   export ECHO_MODE=REPLAY
   export ECHO_SESSION_ID=user-service-test
   ```

2. **Restart the proxy:**
   ```bash
   docker-compose restart echo-proxy
   ```

3. **Make the same requests - get recorded responses:**
   ```bash
   # Returns the recorded response, no real service needed!
   curl http://localhost:8080/api/users
   ```

## 🎯 Use Cases

### 1. Integration Testing
Record production-like traffic once, replay it in CI/CD for fast, deterministic tests.

### 2. Local Development
Develop against recorded traffic without spinning up 10 dependent services.

### 3. Debugging
Capture problematic production requests and replay them locally for investigation.

### 4. Performance Testing
Isolate service performance by removing network dependency on downstream services.

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.2, Spring Cloud Gateway |
| **Build** | Gradle 8.5 (Multi-project) |
| **Database** | PostgreSQL 15 |
| **Messaging** | RabbitMQ 3.12 |
| **ORM** | Spring Data JPA (Hibernate) |
| **Migrations** | Liquibase |
| **Containerization** | Docker & Docker Compose |
| **Testing** | JUnit 5, Mockito |

## 📚 API Documentation

### List all sessions
```http
GET /api/v1/sessions
```

### Get traffic for a session
```http
GET /api/v1/sessions/{sessionId}/traffic
```

**Response Example:**
```json
[
  {
    "id": 1,
    "sessionId": "user-service-test",
    "method": "GET",
    "path": "/api/users",
    "statusCode": 200,
    "responseBody": "{\"users\": [...]}",
    "timestamp": "2025-09-30T10:15:30Z"
  }
]
```

Full API documentation: [docs/API.md](docs/API.md)

## 🔧 Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `ECHO_MODE` | `RECORD` | Operating mode: `RECORD` or `REPLAY` |
| `ECHO_SESSION_ID` | `default-session` | Session identifier for grouping traffic |
| `ECHO_TARGET_URL` | `http://localhost:9000` | Target service (RECORD mode) |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `RABBITMQ_HOST` | `localhost` | RabbitMQ host |

See [docs/CONFIGURATION.md](docs/CONFIGURATION.md) for full configuration options.

## 🧪 Development

### Project Structure
```
echo-recorder/
├── backend/
│   ├── echo-proxy/          # Spring Cloud Gateway
│   ├── ingestor-service/    # Data persistence
│   └── echo-api/            # REST API
├── docs/                    # Documentation
├── build.gradle             # Root Gradle config
├── docker-compose.yml       # Development stack
└── README.md
```

### Building Locally
```bash
# Build all services
./gradlew build

# Run tests
./gradlew test

# Run specific service
./gradlew :backend:echo-proxy:bootRun
```

### Running Tests
```bash
# All tests
./gradlew test

# Specific service
./gradlew :backend:echo-api:test

# With coverage
./gradlew test jacocoTestReport
```

## 🤝 Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on:
- Code of conduct
- Development setup
- Pull request process
- Coding conventions

## 📋 Roadmap

**What's Working Now (v1.0)**
- [x] Core recording/replay functionality
- [x] Session management
- [x] REST API for querying traffic
- [x] Docker Compose setup
- [x] Database migrations with Liquibase

**Ideas I'm Considering**
- [ ] Simple web UI for viewing traffic (would be nice to have)
- [ ] Better filtering/search capabilities
- [ ] Session export/import
- [ ] Request/response editing

**Maybe Someday**
- [ ] Kubernetes examples (if there's interest)
- [ ] gRPC support (would need to learn more about this first)
- [ ] More documentation and examples

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

Built with:
- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway) - API Gateway
- [RabbitMQ](https://www.rabbitmq.com/) - Message broker
- [PostgreSQL](https://www.postgresql.org/) - Database

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/jlapugot/echo-recorder/issues)
- **Discussions:** [GitHub Discussions](https://github.com/jlapugot/echo-recorder/discussions)
- **Documentation:** [docs/](docs/)

---

**Star ⭐ this repo if you find it useful!**

Made with ❤️ by the Echo Platform community
