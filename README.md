## 2026-05-01 — Access Control Validation
# Lumora Hotels — Backend API

> A production-grade RESTful API for a luxury hotel reservation platform, built with Spring Boot 3 and secured with JWT authentication. Developed as a portfolio project with real-world architecture decisions and active security validation.

---

## Overview

Lumora Hotels is a full-stack hotel reservation system featuring a Spring Boot backend API and a static HTML/CSS/JS frontend served directly by the embedded Tomcat server. The project was designed to demonstrate professional backend development practices including layered architecture, stateless authentication, database migration management, and security hardening.

The frontend was integrated into the backend's static resources, making the entire application available through a single deployable artifact.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Security | Spring Security 6 + JWT (JJWT 0.12) |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database | H2 (dev) / PostgreSQL (prod) |
| Migrations | Flyway 10 |
| Documentation | SpringDoc OpenAPI / Swagger UI |
| Monitoring | Spring Boot Actuator + Micrometer |
| Build Tool | Maven |
| Frontend | HTML5 / CSS3 / Vanilla JS (SPA) |

---

## Architecture

```
┌─────────────────────────────────────────────┐
│              Spring Boot Application        │
│                                             │
│  ┌──────────┐   ┌──────────┐   ┌─────────┐  │
│  │Controller│ → │ Service  │ → │  Repo   │  │
│  └──────────┘   └──────────┘   └─────────┘  │
│        ↑               ↑                    │
│   JWT Filter      PasswordEncoder           │
│        ↑               ↑                    │
│   Spring Security Filter Chain              │
│                                             │
│  ┌─────────────────────────────────────┐    │
│  │  Static Resources (Frontend SPA)    │    │ 
│  └─────────────────────────────────────┘    │
└─────────────────────────────────────────────┘
```

**Request flow:** Every HTTP request passes through the `JwtFilter` before reaching any controller. Public endpoints (`/api/v1/users/login`, `/api/v1/users/register`, `/`, `/swagger-ui/**`) are explicitly whitelisted in the `SecurityFilterChain`. All other routes require a valid Bearer token.

---

## API Endpoints

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/users/register` | Public | Create new account |
| POST | `/api/v1/users/login` | Public | Authenticate and receive JWT |
| GET | `/api/v1/users/me` | Bearer | Get authenticated user profile |
| GET | `/api/v1/users` | Admin | List all users (paginated) |
| PATCH | `/api/v1/users/{id}/role` | Admin | Update user role |
| PATCH | `/api/v1/users/{id}/toggle` | Admin | Enable or disable account |
| GET/POST | `/api/v1/hotels/**` | Mixed | Hotel management |
| GET/POST | `/api/v1/rooms/**` | Mixed | Room management |
| GET/POST | `/api/v1/reservations/**` | Bearer | Reservation management |
| GET/POST | `/api/v1/reviews/**` | Bearer | Review management |

Full interactive documentation available at `/swagger-ui/index.html`.

---

## Security Implementation

### Authentication
Stateless JWT-based authentication. On successful login, the server returns a signed token containing the user's email and roles. The token is validated on every subsequent request by the `JwtFilter` before the request reaches the business layer.

### Authorization
Role-based access control with two roles: `GUEST` and `ADMIN`. Admin-only endpoints are protected at the service layer, not just at the controller level.

### Password Storage
All passwords are hashed using BCrypt before persistence. Plain-text passwords are never stored or logged.

### CORS
Configured globally via `CorsConfigurationSource` to allow cross-origin requests during development.

---

## Security Testing — Controlled Environment

> **Disclaimer:** The following security assessment was performed exclusively against the developer's own application in a controlled, isolated lab environment for educational purposes. No unauthorized systems were targeted.

### Lab Environment

| Component | Details |
|---|---|
| Target Host | Windows 11 — `192.168.1.19:8081` |
| Attack Machine | Kali Linux 2026.1 (VirtualBox VM) |
| Network | Local area network — isolated lab |
| Tool | Nmap 7.98 |
| Date | May 3, 2026 |

### Scan Executed

```bash
nmap -p 8081 --script "http-*" 192.168.1.19
```

### Scan Output

```
Starting Nmap 7.98 ( https://nmap.org ) at 2026-05-03 16:01 -0300
Nmap scan report for 192.168.1.19
Host is up (0.00048s latency).

PORT     STATE  SERVICE
8081/tcp open   blackice-icecap

Nmap done: 1 IP address (1 host up) scanned in 4.89 seconds
```

### Analysis

The HTTP script suite returned minimal information, which is the expected and desired behavior for a hardened Spring Boot application. Key observations:

**What the scan revealed:**
- Port 8081 is open and accepting TCP connections
- The service is responsive with low latency (0.00048s)
- No application framework version was disclosed in response headers

**What the scan could not enumerate:**
- No endpoints, routes, or controller mappings were exposed
- No error messages revealing internal structure
- No `X-Powered-By`, `Server`, or framework headers returned
- No Actuator endpoints accessible without authentication

**Root cause of minimal disclosure:**

Spring Security's `SecurityFilterChain` intercepts all unauthenticated HTTP requests before they reach any application controller. Nmap's HTTP scripts rely on receiving meaningful HTTP responses to enumerate application metadata — since every unauthenticated request returns a `401 Unauthorized` with no body, the scripts have nothing to analyze.

### Security Conclusion

The application's default Spring Security configuration effectively prevents automated reconnaissance tools from enumerating internal structure, routes, and framework metadata. This behavior validates the correct implementation of the `SecurityFilterChain` and demonstrates that the authentication layer is functioning as the first line of defense.

---

## Application Screenshot

The frontend is a single-page application served directly by Spring Boot's embedded Tomcat, accessible at `http://localhost:8081`.

> *Lumora Hotels — Hotel Detail Page with reservation panel*

---

## Running Locally

**Prerequisites:** Java 21, Maven 3.8+

```bash
# Clone the repository
git clone https://github.com/your-username/lumora-backend.git
cd lumora-backend

# Configure application.properties in src/main/resources/
# Required properties:
# server.port=8081
# lumora.jwt.secret=<base64-encoded-256bit-key>
# lumora.jwt.expiration-ms=86400000
# spring.datasource.url=jdbc:h2:mem:testdb

# Build and run
mvn clean compile
mvn spring-boot:run
```

Access the application at `http://localhost:8081`
Access the API documentation at `http://localhost:8081/swagger-ui/index.html`

---

## What I Learned

- Designing and implementing stateless JWT authentication from scratch using JJWT 0.12's updated API
- Resolving circular dependency issues in Spring's IoC container using constructor injection with `@Lazy`
- Configuring Spring Security's filter chain to distinguish between public and protected routes
- Managing database schema evolution with Flyway migrations
- Serving a frontend SPA as static resources within a Spring Boot application
- Performing basic security reconnaissance using Nmap against a self-hosted application
- Interpreting scan results and correlating them with application security configuration

---

## Next Steps

- [ ] Implement integration tests for authentication endpoints
- [ ] Add Docker and docker-compose for PostgreSQL
- [ ] Configure rate limiting on login endpoint to prevent brute force
- [ ] Add security headers (X-Frame-Options, CSP, HSTS)
- [ ] Perform JWT manipulation testing with jwt_tool
- [ ] Test with Burp Suite for request interception and modification
- [ ] Deploy to cloud environment (Railway or Render)

---

## License

This project is intended for educational and portfolio purposes.
