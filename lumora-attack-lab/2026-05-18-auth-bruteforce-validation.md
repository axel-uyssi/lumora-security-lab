# Authentication Brute Force Validation — 2026-05-18

## Objective

Validate the defensive behavior of the Lumora Hotels API authentication
pipeline against automated brute force attempts using a controlled
offensive simulation framework.

This test was executed in a local and isolated laboratory environment
for defensive validation purposes only.

---

# Environment

## Target API

- Project: Lumora Hotels Backend
- Stack: Spring Boot 3 + Spring Security + JWT
- Endpoint:
  http://localhost:9999/api/v1/auth/login

## Offensive Framework

- Module: lumora-attack-lab
- Language: Python 3.14
- Attack Type:
  - Brute Force Authentication Simulation

---

# Test Scenario

The simulator executed multiple concurrent authentication attempts
against the login endpoint using a controlled password wordlist.

### Target Account

```txt
admin@test.com
Password Wordlist
123456
admin123
password
Spring@2025
senha123
Request Structure
HTTP Method
POST
Payload
{
  "email": "admin@test.com",
  "password": "<candidate_password>"
}
Framework Architecture

The offensive simulation framework uses a modular architecture composed of:

lumora-attack-lab/
│
├── attacks/
│   ├── brute_force.py
│   ├── credential_stuffing.py
│   └── password_spray.py
│
├── core/
│   ├── engine.py
│   ├── workers.py
│   └── queue_manager.py
│
├── models/
│   └── task.py
│
├── utils/
│   ├── logger.py
│   ├── metrics.py
│   └── report.py

Observed Result
API Response
HTTP 403 FORBIDDEN
Simulator Output
[FORBIDDEN] admin@test.com:123456
[FORBIDDEN] admin@test.com:admin123
[FORBIDDEN] admin@test.com:password
[FORBIDDEN] admin@test.com:Spring@2025
[FORBIDDEN] admin@test.com:senha123
Analysis

The API rejected all authentication attempts before reaching the
authentication provider layer.

This behavior indicates that Spring Security CSRF protection was active,
blocking unauthorized POST requests that did not contain a valid CSRF token.

The requests were intercepted before:

JWT generation
credential validation
UserDetailsService execution
authentication manager processing

This demonstrates that the security filter chain correctly identified
and rejected non-trusted state-changing requests.

Security Implications
Positive Findings
Spring Security protection active
Unauthorized POST requests blocked
Authentication endpoint not directly exposed
Defensive middleware functioning correctly
Attack automation prevented at filter level
Important Observation

Although CSRF protection blocked the attack simulation successfully,
modern REST APIs commonly disable CSRF when using:

JWT authentication
stateless sessions
Bearer Token authorization

Therefore, CSRF alone should not be considered sufficient protection
against brute force attacks in production-grade APIs.

Recommended Defensive Enhancements
Recommended Controls
1. Rate Limiting

Example:

5 requests/second per IP
2. Account Lockout

Example:

5 failed attempts
→ temporary account lock
3. Progressive Delays

Example:

1s → 2s → 4s response delay
4. Monitoring & Alerting

Track:

repeated authentication failures
IP abuse patterns
credential stuffing behavior
abnormal login frequency

5. MFA Enforcement

Even if credentials are compromised,
secondary authentication mitigates account takeover.

Action Taken
Current State

CSRF protection remains enabled intentionally during this validation phase
to observe the native defensive behavior of Spring Security.

Future tests will include:

CSRF-disabled scenarios
rate limiting validation
authentication throttling
lockout mechanism testing
JWT abuse simulation
concurrent attack analysis

Conclusion

The Lumora Hotels API demonstrated correct defensive behavior by rejecting
unauthorized automated authentication requests through Spring Security's
CSRF protection layer.

The offensive simulation framework successfully validated that the
authentication pipeline is protected before credential processing occurs.

This laboratory exercise confirms the effectiveness of the current
security filter chain while also highlighting the importance of additional
modern brute force protections for stateless REST architectures.
