# Security Policy

## Supported Versions

We actively support the following versions of Echo Platform with security updates:

| Version | Supported          |
| ------- | ------------------ |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub issues.**

If you discover a security vulnerability in Echo Platform, please report it privately using one of these methods:

### Preferred Method: GitHub Security Advisory
1. Go to the repository's Security tab
2. Click "Report a vulnerability"
3. Fill out the advisory form with details

### Alternative: Email
Send an email to: **[your-email@example.com]** (replace with your actual security contact)

**Please include:**
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Affected versions
- Suggested fix (if any)

## Response Timeline

- **Initial Response:** Within 48 hours
- **Status Update:** Within 7 days
- **Fix Timeline:** Depends on severity
  - Critical: 7-14 days
  - High: 14-30 days
  - Medium: 30-60 days
  - Low: Best effort

## Disclosure Policy

- We will acknowledge your report within 48 hours
- We will work with you to understand and validate the issue
- We will develop and test a fix
- We will release the fix and publicly disclose the vulnerability
- We will credit you for the discovery (unless you prefer to remain anonymous)

## Security Best Practices

When deploying Echo Platform:

### Production Deployment
- [ ] Use environment variables for sensitive configuration
- [ ] Enable authentication on echo-api endpoints
- [ ] Use TLS/SSL for all HTTP traffic
- [ ] Restrict database access to internal network only
- [ ] Use secrets management (Vault, AWS Secrets Manager, etc.)
- [ ] Regularly update dependencies
- [ ] Review and redact sensitive data before recording

### Network Security
- [ ] Deploy services in private networks
- [ ] Use network policies (Kubernetes) or security groups
- [ ] Enable RabbitMQ authentication
- [ ] Restrict PostgreSQL to internal access only

### Data Privacy
- [ ] Review captured traffic for PII/sensitive data
- [ ] Implement data retention policies
- [ ] Consider encryption at rest for database
- [ ] Audit session access logs

## Known Limitations (Development Mode)

The default Docker Compose setup is designed for **local development only** and includes:
- Default credentials (guest/guest for RabbitMQ)
- No authentication on API endpoints
- No TLS encryption
- Exposed ports

**Never use the default configuration in production.**

## Security Updates

Security updates will be:
- Published as GitHub Security Advisories
- Documented in CHANGELOG.md
- Tagged with `security` label
- Backported to supported versions when possible

## Hall of Fame

We appreciate security researchers who responsibly disclose vulnerabilities. With permission, we'll list contributors here:

- (No vulnerabilities reported yet)

---

Thank you for helping keep Echo Platform and its users safe!