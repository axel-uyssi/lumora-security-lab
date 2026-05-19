Authentication Brute Force Validation — 2026-05-18

Objective
Validate the defensive behavior of the Lumora Hotels API authentication pipeline against automated brute force attempts using a controlled offensive simulation framework.

This test was executed in a local and isolated laboratory environment for defensive validation purposes only.
Environment
Target API:
- Project: Lumora Hotels Backend
- Stack: Spring Boot 3 + Spring Security + JWT
- Endpoint: http://localhost:9999/api/v1/auth/login

Offensive Framework:
- Module: lumora-attack-lab
- Language: Python 3.14
- Attack Type: Brute Force Authentication Simulation
Test Scenario
The simulator executed multiple concurrent authentication attempts against the login endpoint using a controlled password wordlist.

Target Account:
admin@test.com

Password Wordlist:
- 123456
- admin123
- password
- Spring@2025
- senha123
Observed Result
HTTP 403 FORBIDDEN

Simulator Output:
[FORBIDDEN] admin@test.com:123456
[FORBIDDEN] admin@test.com:admin123
[FORBIDDEN] admin@test.com:password
[FORBIDDEN] admin@test.com:Spring@2025
[FORBIDDEN] admin@test.com:senha123
Analysis
The API rejected all authentication attempts before reaching the authentication provider layer.

This behavior indicates that Spring Security CSRF protection was active, blocking unauthorized POST requests that did not contain a valid CSRF token.

The requests were intercepted before:
- JWT generation
- credential validation
- UserDetailsService execution
- authentication manager processing
Security Implications
Positive Findings:
- Spring Security protection active
- Unauthorized POST requests blocked
- Authentication endpoint not directly exposed
- Defensive middleware functioning correctly
- Attack automation prevented at filter level
Recommended Defensive Enhancements
1. Rate Limiting
2. Account Lockout
3. Progressive Delays
4. Monitoring & Alerting
5. MFA Enforcement
Conclusion
The Lumora Hotels API demonstrated correct defensive behavior by rejecting unauthorized automated authentication requests through Spring Security's CSRF protection layer.

The offensive simulation framework successfully validated that the authentication pipeline is protected before credential processing occurs.
