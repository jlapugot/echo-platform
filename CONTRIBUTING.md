# Contributing to Echo Platform

First off, thank you for considering contributing to Echo! It's people like you that make Echo such a great tool.

## Code of Conduct

This project and everyone participating in it is governed by our commitment to providing a welcoming and inclusive environment. By participating, you are expected to uphold this code.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When you create a bug report, include as many details as possible:

* **Use a clear and descriptive title**
* **Describe the exact steps to reproduce the problem**
* **Provide specific examples** (code snippets, curl commands, etc.)
* **Describe the behavior you observed** and what you expected to see
* **Include logs and error messages**
* **Specify your environment** (OS, Java version, Docker version)

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, include:

* **Use a clear and descriptive title**
* **Provide a detailed description** of the suggested enhancement
* **Explain why this enhancement would be useful** to most Echo users
* **List examples** of how this feature would be used

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Follow the project structure** and coding conventions
3. **Write tests** for your changes
4. **Ensure the test suite passes** (`./gradlew test`)
5. **Update documentation** as needed
6. **Write clear commit messages** following the format below

#### Commit Message Format

```
type(scope): subject

body (optional)

footer (optional)
```

**Types:**
* `feat`: A new feature
* `fix`: A bug fix
* `docs`: Documentation only changes
* `style`: Code style changes (formatting, missing semi colons, etc)
* `refactor`: Code refactoring
* `test`: Adding missing tests
* `chore`: Changes to build process or auxiliary tools

**Example:**
```
feat(echo-api): add endpoint for deleting sessions

Add DELETE /api/v1/sessions/{sessionId} endpoint to allow
users to remove old recording sessions.

Closes #123
```

## Development Setup

### Prerequisites

* Java 17+
* Docker & Docker Compose
* Gradle 8.5+

### Local Development

1. **Clone your fork:**
   ```bash
   git clone https://github.com/YOUR_USERNAME/echo-recorder.git
   cd echo-recorder
   ```

2. **Start the infrastructure:**
   ```bash
   docker-compose up postgres rabbitmq
   ```

3. **Run services locally:**
   ```bash
   # Terminal 1 - Ingestor Service
   ./gradlew :backend:ingestor-service:bootRun

   # Terminal 2 - Echo API
   ./gradlew :backend:echo-api:bootRun

   # Terminal 3 - Echo Proxy
   ./gradlew :backend:echo-proxy:bootRun
   ```

4. **Run tests:**
   ```bash
   ./gradlew test
   ```

### Project Structure

```
echo-platform/
├── backend/
│   ├── echo-proxy/          # Gateway service
│   ├── ingestor-service/    # Data persistence
│   └── echo-api/            # REST API
├── docs/                    # Additional documentation
├── build.gradle             # Root build configuration
└── docker-compose.yml       # Local development stack
```

## Coding Conventions

### Java

* Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
* Use Lombok annotations to reduce boilerplate
* Write Javadoc for public APIs
* Use meaningful variable and method names
* Keep methods small and focused (< 50 lines ideally)

### Testing

* Write unit tests for all business logic
* Use Mockito for mocking dependencies
* Aim for 80%+ code coverage
* Use descriptive test method names: `methodName_shouldDoSomething_whenCondition()`

### Git Workflow

1. Create a feature branch: `git checkout -b feat/my-feature`
2. Make your changes with clear commits
3. Push to your fork: `git push origin feat/my-feature`
4. Create a Pull Request against `main`
5. Wait for review and address feedback

## Review Process

* All submissions require review before merging
* Reviewers will check:
  * Code quality and style
  * Test coverage
  * Documentation
  * Performance implications
  * Security considerations

## Questions?

Feel free to open an issue with the `question` label, or reach out to the maintainers.

## License

By contributing, you agree that your contributions will be licensed under the MIT License.