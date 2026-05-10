# Spring Security HTTP Hardening Assessment

## Nikto-Based Security Evaluation & Response Header Analysis

### Objective

Evaluate the current Spring Security hardening posture of the web application running on port 8081 using HTTP enumeration, response header inspection, and browser-side security validation techniques.

The assessment focuses on:

* HTTP security headers
* Browser hardening
* Access control behavior
* CORS exposure
* Secure response configuration
* Spring Security integration

---

# Environment Information

| Field                 | Value                                   |
| --------------------- | --------------------------------------- |
| Target Host           | 192.168.1.156                           |
| Service               | Web Application / API                   |
| Backend               | Spring Boot / Apache Tomcat             |
| Port                  | 8081                                    |
| Operating Environment | WSL / Docker Bridge                     |
| Tool Used             | Nikto v2.6.0                            |
| Validation Tools      | curl, Nmap                              |
| Assessment Type       | Authorized Internal Security Assessment |

---

# Assessment Summary

The application demonstrates partial Spring Security hardening and properly restricts unauthorized administrative access through HTTP 403 responses.

The environment also suppresses direct server banner disclosure and implements some default Spring Security protections, including MIME sniffing prevention and aggressive cache disabling.

However, several modern browser security protections remain absent or incompletely configured, including:

* Content-Security-Policy (CSP)
* Referrer-Policy
* Permissions-Policy
* Strict-Transport-Security (HSTS)

Additionally, the application exposes a permissive Cross-Origin Resource Sharing (CORS) configuration using wildcard origins.

---

# HTTP Response Validation

## Root Application Response

### Command

```bash
curl -I http://192.168.1.156:8081
```

### Response

```http
HTTP/1.1 200
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Content-Type: text/html;charset=ISO-8859-1
```

---

# Access Control Validation

## Restricted Administrative Endpoint

### Command

```bash
curl -i http://192.168.1.156:8081/admin
```

### Response

```http
HTTP/1.1 403
Content-Length: 0
```

---

# Security Observations

## Positive Hardening Indicators

### Administrative Access Restriction

The `/admin` route correctly returns:

```http
403 Forbidden
```

This behavior indicates:

* Spring Security authorization enforcement
* Route-level access protection
* Proper denial of unauthorized requests
* Absence of backend exception leakage

---

## MIME Sniffing Protection Enabled

The application exposes:

```http
X-Content-Type-Options: nosniff
```

This reduces the risk of browser MIME-type confusion attacks.

---

## Cache Hardening Enabled

The application correctly disables browser-side caching:

```http
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

This is considered a strong security practice for authenticated or dynamic applications.

---

## Reduced Server Fingerprinting

Nikto did not retrieve a direct server banner:

```text
Server: No banner retrieved
```

This reduces passive fingerprinting opportunities during reconnaissance.

---

# Missing Security Controls

## [VULN-001] Missing Content-Security-Policy (CSP)

### Severity

Medium

### Risk

Without CSP enforcement, the application remains more exposed to:

* Cross-Site Scripting (XSS)
* Script injection
* Malicious external resource loading

### Recommendation

Implement restrictive CSP rules globally.

### Suggested Configuration

```java
.contentSecurityPolicy(csp ->
    csp.policyDirectives("default-src 'self'")
)
```

---

## [VULN-002] Missing Referrer-Policy

### Severity

Low

### Risk

Internal URL structures and navigation metadata may leak through browser referrer headers.

### Recommendation

```http
Referrer-Policy: strict-origin-when-cross-origin
```

---

## [VULN-003] Missing Permissions-Policy

### Severity

Low

### Risk

Browser features such as camera, microphone, and geolocation are not explicitly restricted.

### Recommendation

```http
Permissions-Policy: camera=(), microphone=(), geolocation=()
```

---

## [VULN-004] Missing Strict-Transport-Security (HSTS)

### Severity

Medium

### Risk

The application currently operates over HTTP and does not enforce HTTPS transport protection.

### Recommendation

Enable HTTPS and configure HSTS globally.

---

## [VULN-005] Permissive CORS Configuration

### Severity

Medium

### Observed Behavior

Nikto identified:

```http
Access-Control-Allow-Origin: *
```

### Risk

Wildcard CORS policies may allow unintended third-party browser interactions with the application.

### Recommendation

Restrict origins to trusted domains only.

---

# Spring Security Hardening Configuration

## Recommended Configuration

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .headers(headers -> headers

            .frameOptions(frame ->
                frame.deny()
            )

            .contentSecurityPolicy(csp ->
                csp.policyDirectives("default-src 'self'")
            )

            .referrerPolicy(referrer ->
                referrer.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy
                        .STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                )
            )

            .permissionsPolicy(policy ->
                policy.policy(
                    "camera=(), microphone=(), geolocation=()"
                )
            )

            .httpStrictTransportSecurity(hsts ->
                hsts.includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
            )

            .contentTypeOptions(withDefaults())
        )

        .requiresChannel(channel ->
            channel.anyRequest().requiresSecure()
        );

    return http.build();
}
```

---

# Hardening Checklist

* [x] Administrative access restriction functioning
* [x] MIME sniffing protection enabled
* [x] Cache-control hardening enabled
* [x] Reduced server fingerprint exposure
* [ ] Implement Content-Security-Policy
* [ ] Configure Referrer-Policy
* [ ] Configure Permissions-Policy
* [ ] Enable HTTPS
* [ ] Enable HSTS
* [ ] Restrict CORS origins
* [ ] Migrate charset to UTF-8

---

# Validation Commands

## Nikto Security Validation

```bash
nikto -h http://192.168.1.156:8081
```

---

## HTTP Header Verification

```bash
curl -I http://192.168.1.156:8081
```

---

## Access Control Verification

```bash
curl -i http://192.168.1.156:8081/admin
```

---

## Security Header Enumeration

```bash
nmap --script http-security-headers -p 8081 192.168.1.156
```

---

# Assets Structure

```bash
mkdir -p docs/security/assets/screenshots

mkdir -p docs/security/assets/evidence

mkdir -p docs/security/assets/diagrams
```

---

## Suggested Evidence Files

```bash
touch docs/security/assets/screenshots/nikto-scan-output.png

touch docs/security/assets/screenshots/http-403-admin-response.png

touch docs/security/assets/screenshots/curl-security-headers.png

touch docs/security/assets/evidence/nikto-output.txt

touch docs/security/assets/evidence/curl-headers.txt

touch docs/security/assets/evidence/nmap-security-headers.txt

touch docs/security/assets/evidence/admin-endpoint-response.txt

touch docs/security/assets/diagrams/spring-security-flow.png
```

---

# Conclusion

The application demonstrates an intermediate hardening posture with functional access restriction mechanisms and partial Spring Security protections already enabled.

The `/admin` endpoint correctly enforces authorization boundaries through HTTP 403 responses without exposing sensitive backend information, indicating that access control policies are functioning as intended.

However, several modern browser security controls remain absent, including CSP, HSTS, Referrer Policy, and Permissions Policy. The current wildcard CORS configuration also increases browser-side exposure.

Although no critical vulnerabilities were identified during this assessment, implementing modern HTTP security headers and enforcing HTTPS are strongly recommended to improve the application's overall defensive posture and align with modern secure configuration standards.
