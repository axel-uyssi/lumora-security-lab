#  Lumora Security Lab

**Lumora Security Lab** is a controlled, containerized environment designed for the analysis, simulation, and validation of security vulnerabilities in web applications, with a primary focus on authentication mechanisms and API security.

This project is structured as a **hands-on laboratory**, enabling reproducible security experiments and controlled attack simulations for educational and research purposes.

---

##  Purpose

The main objective of this project is to provide a realistic environment for:

* Systematic analysis of common web vulnerabilities
* Controlled execution of attack scenarios
* Evaluation of system behavior under adversarial conditions
* Implementation and validation of defensive mechanisms

---

##  Project Architecture

```plaintext
lumora-security-lab/
│
├── docs/security        # Security test cases and vulnerability documentation
├── nginx                # Reverse proxy configuration
├── src                  # Application source code (Spring Boot)
├── docker-compose.yml   # Multi-container orchestration
├── pom.xml              # Maven build configuration
```

The architecture is designed to simulate a **production-like environment**, including reverse proxying and service isolation via containers.

---

##  Technology Stack

* **Java (Spring Boot)** – Backend application layer
* **Docker & Docker Compose** – Environment orchestration and isolation
* **Nginx** – Reverse proxy and traffic control
* **JWT (JSON Web Token)** – Authentication mechanism
* **Maven** – Dependency management and build automation

---

##  Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/lumora-security-lab.git
cd lumora-security-lab
```

### 2. Build and start the environment

```bash
docker-compose up --build
```

### 3. Access the application

```
http://localhost:8080
```

---

## 🧪 Security Testing Scenarios

The lab is designed to support multiple categories of security testing.

---

###  Brute Force Attack Simulation

This scenario evaluates the system's resilience against repeated authentication attempts.

**Test approach:**

* Perform multiple login attempts using invalid credentials
* Automate requests using tools such as:

  * Hydra
  * Burp Suite
  * Postman (manual testing)

**Key evaluation points:**

* Absence of account lockout mechanisms
* Lack of rate limiting
* No progressive delay between attempts

**Expected insight:**
Identification of weaknesses in authentication hardening strategies.

---

###  JWT Security Analysis

This scenario focuses on the robustness of token-based authentication.

**Test vectors:**

* Token reuse and session persistence
* Token expiration validation
* Payload manipulation (tampering attempts)

**Recommended tools:**

* jwt.io
* Postman

**Expected insight:**
Assessment of token integrity, validation logic, and potential trust flaws.

---

###  API Security Assessment

This scenario evaluates general API security posture.

**Key checks:**

* Unauthorized access to protected endpoints
* Improper input validation
* Exposure of sensitive data

**Expected insight:**
Detection of misconfigurations and insecure API design patterns.

---

##  Experimental Capabilities

The lab can be extended to support more advanced scenarios:

### Defensive Mechanisms

* Rate limiting strategies
* Account lockout policies
* CAPTCHA integration
* Multi-factor authentication (2FA)

### Additional Attack Simulations

* SQL Injection (SQLi)
* Cross-Site Scripting (XSS)
* Cross-Site Request Forgery (CSRF)
* Token hijacking

### Observability & Monitoring

* Attack logging and traceability
* Security event monitoring
* Real-time dashboards

---

## 🔬 Analytical Perspective

Lumora Security Lab is not just a vulnerable application, but a **controlled experimentation platform**, where each vulnerability can be:

1. Reproduced consistently
2. Measured in terms of system impact
3. Mitigated and re-tested

This enables a **closed feedback loop**, essential for deep security learning and validation.

---

##  Disclaimer

This project is intended strictly for **educational and research purposes**.

* Do not use these techniques against systems without explicit authorization
* All tests should be conducted in controlled environments only

---

##  Author

Axel Uyssi

---

##  Contributions

Contributions are encouraged, especially in:

* New attack scenarios
* Defensive implementations
* Security documentation improvements
* Testing methodologies

---
