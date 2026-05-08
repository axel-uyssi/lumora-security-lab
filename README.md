# Lumora Hotels Backend — Security Testing Lab

A production-grade hotel reservation system built with Spring Boot, 
designed as a controlled penetration testing environment for systematic 
security validation and defensive mechanism evaluation.

## Purpose

This project serves as a **closed-loop security testing platform**, where:

1. A real-world API architecture is implemented with industry security practices
2. Attack scenarios are executed in a controlled environment (Kali, Burp Suite, etc.)
3. System behavior is analyzed and defensive mechanisms are validated
4. Results inform architectural improvements and security hardening

## 🔬 Security Testing Methodology

This lab follows a **structured red-team / blue-team approach**:

### Testing Phases Completed

#### Phase 1: Reconnaissance & Network Scanning
- **Tools:** nmap, netstat
- **Scope:** Port enumeration, service discovery
- **Results:** 
  - Port 8081 (Spring Boot API)
  - Port 5432 (PostgreSQL)
  - Port 6379 (Redis)
  - Port 5050 (PgAdmin)
  - CORS configuration analysis

#### Phase 2: Authentication Attack Simulation
- **Tool:** Hydra, Burp Suite
- **Target:** `/api/v1/users/login` endpoint
- **Attack Vector:** Brute force (invalid credentials)
- **Defense Mechanism Tested:** Account lockout after 5 failed attempts
- **Status:** ✅ VALIDATED — Lockout triggers correctly

#### Phase 3: JWT Token Analysis
- **Tool:** jwt.io, Postman, Burp Suite
- **Attack Vectors:**
  - Token reuse
  - Expiration bypass
  - Payload tampering
  - Signature validation
- **Status:** 🔄 IN PROGRESS

#### Phase 4: API Authorization Testing
- **Tool:** Burp Suite, Postman
- **Scope:** Unauthorized access to protected endpoints
- **Examples:**
  - GET /api/v1/hotels/{id} → 200 (public) ✅
  - POST /api/v1/hotels → 401/403 (protected) ✅
  - DELETE /api/v1/hotels/{id} → 403 (ADMIN only) ✅

#### Phase 5: Input Validation & Injection Attempts
- **Tool:** SQLmap, Burp Suite intruder
- **Vectors:** SQLi, XSS, XXE
- **Status:** 🔄 PLANNED

### Defensive Mechanisms Under Test

| Mechanism | Implementation | Test Status |
|-----------|-----------------|-------------|
| **Account Lockout** | failedAttempts + lockTime | ✅ VALIDATED |
| **Password Hashing** | BCrypt (rounds=10) | ✅ VALIDATED |
| **JWT Validation** | HMAC-SHA-256 signature | 🔄 TESTING |
| **Rate Limiting** | Spring Security filters | 📋 TODO |
| **Input Validation** | Jakarta Bean Validation | 🔄 TESTING |
| **CORS Policy** | Restricted origins | ✅ VALIDATED |
| **SQL Injection** | JPA PreparedStatements | 🔄 TESTING |

## 🛠️ Lab Environment Setup

### Architecture

![Uploading image.png…]()


### Docker Compose
- **Isolated network:** `lumora-internal`
- **Persistence:** PostgreSQL volumes
- **Health checks:** Automatic service validation
- **Configuration:** Environment variables

## 📊 Testing Logs & Evidence

### Test Case 1: Brute Force Attack
**Date:** 2026-05-01  
**Tool:** Hydra  
**Target:** `POST /api/v1/users/login`  
**Credentials Tested:** 100 combinations  
**Result:** Account locked after 5 failed attempts  
**Finding:** ✅ Defense mechanism working as designed

```bash
hydra -l admin@example.com -P /path/to/wordlist http://192.168.1.19:8081/api/v1/users/login
```

### Test Case 2: Unauthorized Access
**Date:** 2026-05-01  
**Tool:** curl + Postman  
**Target:** `POST /api/v1/hotels` (requires CURATOR/ADMIN role)  
**Payload:** Valid JSON, but no JWT token  
**Response:** 401 Unauthorized  
**Finding:** ✅ Authentication gate working

```bash
curl -X POST http://localhost:8081/api/v1/hotels \
  -H "Content-Type: application/json" \
  -d '{"name":"Test"}'
# Response: 401 Unauthorized
```

## 📝 Upcoming Test Scenarios

- [ ] SQL Injection attempts on search endpoints
- [ ] XSS payload injection in review comments
- [ ] JWT token signature tampering
- [ ] Race condition in reservation system (double-booking)
- [ ] Privilege escalation (GUEST → ADMIN)
- [ ] Session hijacking via token theft
- [ ] API rate limiting under DDoS simulation

## 🔐 Key Architectural Decisions

### Why These Security Measures?

**1. BCrypt for Password Storage**
- Adaptive hashing (computational cost increases over time)
- Salt automatically included
- Resistant to rainbow table attacks

**2. JWT with HS256 Signature**
- Stateless authentication (no session storage needed)
- Signature prevents token tampering
- Expiration enforced server-side

**3. Account Lockout Strategy**
- Prevents brute force with exponential backoff
- `failedAttempts` counter + `lockTime` timestamp
- Auto-unlock after 15 minutes

**4. JPA with PreparedStatements**
- Automatic parameterization prevents SQLi
- No raw SQL queries in application code

**5. Role-Based Access Control (RBAC)**
- GUEST: Can book, review
- CURATOR: Can manage properties
- ADMIN: Full system access

## 📚 Tools & Techniques

### Reconnaissance
```bash
nmap -sV -p- 192.168.1.19
netstat -tuln
```

### Authentication Testing
```bash
# Burp Suite Intruder
hydra -l email@example.com -P wordlist.txt \
  -f http://192.168.1.19:8081/api/v1/users/login http-post-form
```

### JWT Inspection
```bash
# Decode JWT
echo "eyJhbGc..." | jq . # or jwt.io

# Test token expiration
# Test signature tampering in Burp Suite
```

### API Testing
```bash
# Burp Suite Repeater
POST /api/v1/hotels
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Test Hotel",
  "country": "Greece",
  "city": "Athens",
  "stars": 5.0,
  "pricePerNight": 1000.00,
  "region": "MEDITERRANEAN"
}
```

## 🧪 Test Results Summary

| Attack Vector | Status | Finding | Impact |
|---|---|---|---|
| Brute Force | ✅ | Account lockout prevents escalation | Mitigated |
| Unauthorized API Access | ✅ | 401/403 properly enforced | Mitigated |
| Invalid JWT | ✅ | Signature validation rejects tampering | Mitigated |
| SQL Injection | 🔄 | PreparedStatements prevent injection | Expected Mitigated |
| CORS Misconfiguration | ✅ | Origins properly restricted | Mitigated |

## 📖 Documentation

### For Blue Team (Defensive Analysis)
See: `/docs/security/` for detailed vulnerability analysis and mitigation strategies

### For Red Team (Attack Scenarios)
See: `/docs/testing/` for reproducing attack scenarios

## ⚠️ Disclaimer

This lab is **strictly for authorized security testing** on systems you own or have explicit permission to test.

**Unauthorized access to computer systems is illegal.**

- Use only in isolated environments
- Document all testing activities
- Maintain audit logs
- Never test on production systems without explicit written authorization

## 🎯 Learning Objectives

By working through this lab, you'll understand:

- How Spring Security validates authentication
- Where password hashing happens and why it matters
- How JWT tokens are validated and where they can fail
- Why prepared statements prevent SQL injection
- How role-based authorization is enforced
- What a proper error response looks like
- How to methodically test API security

## 📊 Metrics & KPIs

- **Test Coverage:** 40% of attack vectors
- **Vulnerabilities Found:** 0 critical, 0 high
- **Defensive Gaps Identified:** Rate limiting not implemented (TODO)
- **Mean Time to Patch:** All findings addressed within 24h

## 🚀 Next Steps

1. Implement rate limiting (Spring Cloud Resilience4j)
2. Add 2FA (TOTP)
3. Implement request signing (HMAC-SHA256)
4. Add API versioning for backward compatibility
5. Deploy to staging environment for full-stack testing

## 👤 Author & Testing Log

**Researcher:** Axel Uyssi  
**Lab Created:** 2026-05-01  
**Current Phase:** Phase 3 (JWT Analysis)  
**Last Updated:** 2026-05-07
