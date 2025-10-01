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

Echo consists of four services in an event-driven architecture:

```
                         ┌─────────────────┐
                         │  echo-dashboard │  ← Web UI (Port 4200)
                         │   (Angular 17)  │
                         └────────┬────────┘
                                  │ HTTP
               ┌──────────────────┴──────────────────┐
               ▼                                     ▼
┌─────────────────┐                         ┌─────────────────┐
│   echo-proxy    │  ← Your app (Port 8080) │   echo-api      │  ← REST API (Port 8082)
│  (Record/Replay)│                         │  (Query/Delete) │
└────────┬────────┘                         └────────┬────────┘
         │ Publishes traffic                         │ Queries
         ▼                                           ▼
┌─────────────────┐                         ┌─────────────────┐
│   RabbitMQ      │  ← Async queue           │   PostgreSQL    │
└────────┬────────┘                         └─────────────────┘
         │ Consumes
         ▼
┌─────────────────┐
│ ingestor-service│
└─────────────────┘
```

### Components

| Service | Technology | Responsibility |
|---------|-----------|----------------|
| **echo-proxy** | Spring Cloud Gateway | Routes traffic (RECORD) or returns mocks (REPLAY) |
| **ingestor-service** | Spring Boot + JPA | Consumes queue, persists to PostgreSQL |
| **echo-api** | Spring Boot REST | Query API for recorded traffic |
| **echo-dashboard** | Angular 17 + Material | Web UI for viewing/managing traffic |

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
- **Dashboard UI**: `http://localhost:4200` - Interactive web interface
- Echo Proxy: `http://localhost:8080` - Proxy endpoint for your services
- Echo API: `http://localhost:8082` - REST API for querying traffic
- RabbitMQ Management: `http://localhost:15672` (guest/guest)

## 📖 Usage Guide

### Using the Dashboard (Recommended)

1. **Open the web interface:**
   ```
   http://localhost:4200
   ```

2. **Switch modes dynamically** using the toggle (no restart needed):
   - **RECORD** (green) - Capture traffic from real services
   - **REPLAY** (orange) - Serve cached responses

3. **Try It page** - Interactive API testing:
   - Enter URL and select HTTP method
   - Send requests through the proxy
   - View formatted responses with headers
   - See mode-specific success messages

4. **View recorded traffic:**
   - Browse sessions and their record counts
   - Inspect individual requests/responses
   - Delete specific records or clear entire sessions

### Recording Traffic (CLI)

1. **Configure the proxy** to point to your real service (in docker-compose.yml):
   ```yaml
   ECHO_MODE: RECORD
   ECHO_SESSION_ID: user-service-test
   ECHO_TARGET_URL: http://your-real-service:9000
   ```

2. **Send requests through Echo:**
   ```bash
   # All requests are forwarded and recorded
   curl http://localhost:8080/api/users
   curl http://localhost:8080/api/users/123
   ```

3. **View recorded traffic:**
   ```bash
   curl http://localhost:8082/api/v1/sessions/user-service-test/traffic | jq
   ```

### Replaying Traffic (CLI)

1. **Switch to REPLAY mode at runtime:**
   ```bash
   curl -X POST http://localhost:8080/api/mode \
     -H "Content-Type: application/json" \
     -d '{"mode":"REPLAY"}'
   ```
   Or update docker-compose.yml and restart.

2. **Make the same requests - get cached responses:**
   ```bash
   # Returns the recorded response instantly, no real service needed!
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
| **Backend Language** | Java 17 |
| **Backend Framework** | Spring Boot 3.2, Spring Cloud Gateway |
| **Frontend** | Angular 17 (Standalone Components), Angular Material |
| **Build** | Gradle 8.5 (Multi-project), Angular CLI |
| **Database** | PostgreSQL 15 |
| **Messaging** | RabbitMQ 3.12 |
| **ORM** | Spring Data JPA (Hibernate) |
| **Migrations** | Liquibase |
| **Containerization** | Docker & Docker Compose |
| **Testing** | JUnit 5, Mockito |

## 📚 API Documentation

### Echo API (Port 8082)

**List all sessions:**
```http
GET /api/v1/sessions
```

**Get traffic for a session:**
```http
GET /api/v1/sessions/{sessionId}/traffic
```

**Delete a specific traffic record:**
```http
DELETE /api/v1/traffic/{id}
```

**Delete all traffic for a session:**
```http
DELETE /api/v1/sessions/{sessionId}/traffic
```

### Echo Proxy Mode Control (Port 8080)

**Get current mode:**
```http
GET /api/mode
```

**Switch mode (runtime, no restart needed):**
```http
POST /api/mode
Content-Type: application/json

{"mode": "RECORD"}  # or "REPLAY"
```

**Response Example:**
```json
{
  "mode": "REPLAY",
  "message": "Mode switched successfully"
}
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
echo-platform/
├── backend/
│   ├── echo-proxy/          # Spring Cloud Gateway
│   ├── ingestor-service/    # Data persistence
│   └── echo-api/            # REST API
├── frontend/                # Angular dashboard
│   ├── src/app/
│   │   ├── components/      # UI components
│   │   ├── services/        # API services
│   │   └── models/          # TypeScript models
│   └── Dockerfile
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
- [x] **Web UI dashboard** (Angular with interactive testing)
- [x] **Runtime mode switching** (no restart required)
- [x] **Delete functionality** (individual records and bulk session delete)
- [x] **CORS support** for frontend integration

**Ideas I'm Considering**
- [ ] Better filtering/search capabilities
- [ ] Session export/import
- [ ] Request/response editing
- [ ] Authentication header matching for replay mode

**Maybe Someday**
- [ ] Kubernetes examples (if there's interest)
- [ ] gRPC support (would need to learn more about this first)
- [ ] Traffic visualization and analytics

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
