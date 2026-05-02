## 2026-05-01 — Access Control Validation

**What I did:**
Performed access tests to the Spring Boot application from an external environment (Kali Linux), sending HTTP requests to the host machine using its local network IP (`192.168.1.19:8081`).
Also validated the application behavior locally using curl requests.

---

**What I understood:**
The application is protected by Spring Security, which correctly restricts access to protected endpoints.
The `403 Forbidden` response indicates that the server is reachable but denies unauthorized access, confirming that access control mechanisms are active.

Additionally:

* Correct port configuration is essential (8081 vs 8080)
* External access depends on proper network exposure
* Security frameworks differentiate between authentication and authorization failures

---

**What did not work:**
Initial attempts failed due to:

* Using the wrong port (8080 instead of 8081)
* Proxy interference (Burp Suite intercepting requests)
* Lack of initial validation using localhost

---

**Fix applied:**

* Corrected the application port to 8081
* Validated server availability locally before external testing
* Adjusted proxy behavior during testing
* Confirmed that the 403 response was expected behavior, not an error

---

**Next step:**

* Intercept authentication flow using Burp Suite
* Analyze login requests and responses
* Test authenticated endpoints
* Explore authorization boundaries and potential vulnerabilities

---
