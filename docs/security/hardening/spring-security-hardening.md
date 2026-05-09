# Spring Security Hardening

## Objective

Implement secure HTTP response headers and application hardening using Spring Security.

---

# Recommended Security Headers

| Header | Recommended Value |
|---|---|
| X-Frame-Options | DENY |
| Content-Security-Policy | default-src 'self' |
| X-Content-Type-Options | nosniff |
| Referrer-Policy | strict-origin-when-cross-origin |
| Strict-Transport-Security | max-age=31536000 |

---

# Spring Security Configuration

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        .headers(headers -> headers
            .frameOptions(frame -> frame.deny())

            .contentSecurityPolicy(csp ->
                csp.policyDirectives("default-src 'self'")
            )

            .referrerPolicy(referrer ->
                referrer.policy(
                    ReferrerPolicyHeaderWriter.ReferrerPolicy
                        .STRICT_ORIGIN_WHEN_CROSS_ORIGIN
                )
            )

            .httpStrictTransportSecurity(hsts ->
                hsts.includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
            )
        )

        .requiresChannel(channel ->
            channel.anyRequest().requiresSecure()
        );

    return http.build();
}
```

---

# Hardening Checklist

- [x] Implement CSP
- [x] Enable HSTS
- [x] Configure Referrer Policy
- [x] Disable MIME sniffing
- [x] Validate secure cookies
- [x] Enforce HTTPS

---

# Validation Commands

```bash
curl -I https://localhost:8443
```

```bash
nmap --script http-security-headers localhost
```

---

# Assets Structure

```bash
touch docs/security/assets/screenshots/ffuf-directory-enum.png

touch docs/security/assets/screenshots/missing-security-headers.png

touch docs/security/assets/evidence/nmap-output.txt

touch docs/security/assets/evidence/ffuf-results.txt

touch docs/security/assets/evidence/headers-response.txt

touch docs/security/assets/diagrams/security-headers-flow.png
```

