# Changelog

All notable changes to Echo Platform will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-10-01 - Open Source Release 🎉

### Added
- **Open Source Release**: Echo Platform is now available to the community under MIT License
- Public documentation: README, CONTRIBUTING, and LICENSE files
- Comprehensive architecture documentation
- Docker Compose setup for easy local development
- Health check endpoints for all services
- Initial GitHub repository setup with issue/PR templates

### Changed
- Renamed internal configuration keys to be more intuitive for external users
- Updated README with community-focused messaging
- Improved error messages for better debugging experience

### Documentation
- Added detailed API documentation
- Created architecture diagrams and system design docs
- Wrote comprehensive getting started guide
- Added real-world usage examples

---

## Version History (Pre-Open Source)

> These are informal versions I used privately while developing this tool.

### [0.9.0] - 2024-09-15 - Stability Improvements

### Added
- Retry logic for RabbitMQ message publishing
- Connection pool optimization for PostgreSQL
- Comprehensive logging for production debugging
- Metrics collection via Spring Boot Actuator

### Fixed
- Memory leak in request body buffering (echo-proxy)
- Race condition in session ID generation
- Database connection timeout issues under heavy load

### [0.8.0] - 2024-07-20 - Performance Tweaks

### Added
- Database indexing for faster queries
- Async processing for traffic ingestion

### Changed
- Switched to reactive programming in echo-proxy (learning experience!)
- Upgraded to Spring Boot 3.2

### Notes
- Made things noticeably faster, especially in replay mode

### [0.7.0] - 2024-05-10 - Stability & Bug Fixes

### Fixed
- Proxy timeout issues with long-running requests
- Incorrect header serialization for multi-value headers
- Session isolation bug causing cross-session data leakage

### Changed
- Increased default connection timeouts
- Improved error handling and recovery

### [0.6.0] - 2024-03-15 - Feature Expansion

### Added
- Query parameter matching in replay mode
- Session metadata (created_at, record_count)
- REST API for listing all available sessions
- Health check improvements

### Changed
- Enhanced matching algorithm for better replay accuracy
- Improved Docker Compose configuration

### [0.5.0] - 2024-01-10 - Database Improvements

### Added
- Liquibase for database schema management
- Database migration scripts
- Composite indexes for query optimization

### Changed
- Migrated from H2 to PostgreSQL for production use
- Normalized database schema

### [0.4.0] - 2023-11-05 - API Enhancements

### Added
- echo-api service for centralized traffic queries
- Internal matching endpoint for replay mode
- REST endpoints for session management

### Changed
- Decoupled query logic from ingestor service
- Improved JSON serialization for headers

### [0.3.0] - 2023-09-01 - Async Architecture

### Added
- RabbitMQ for asynchronous traffic ingestion
- ingestor-service as dedicated data persistence layer
- Message queue configuration and monitoring

### Changed
- Moved from synchronous DB writes to queue-based architecture
- Improved proxy throughput by 10x

### [0.2.0] - 2023-07-01 - REPLAY Mode

### Added
- REPLAY mode functionality
- Matching algorithm for finding recorded responses
- Configuration for switching between RECORD and REPLAY modes

### [0.1.0] - 2023-05-15 - Initial Internal Release

### Added
- echo-proxy service with RECORD mode
- Basic request/response capture
- PostgreSQL persistence
- Docker Compose for local development
- Initial proof of concept

---

## Release Notes

### v1.0.0 - What's New?

This is my first open-source release! I've been using Echo across a few personal projects and decided it was time to clean it up and share it.

**What I've used it for:**
- Testing microservice interactions without running everything locally
- Debugging issues by replaying specific scenarios
- Speeding up development by avoiding service startup time

I'm sharing this in case it's useful to others who face similar problems!

### Upgrade Notes

If you've been using a previous personal version, configuration remains backward compatible.

---

[1.0.0]: https://github.com/jlapugot/echo-platform/releases/tag/v1.0.0
