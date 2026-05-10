Security Assessment Report
HTTP Security Hardening & Web Server Exposure Analysis
Assessment Information
Date: 2026-05-10
Target Host: 192.168.1.156
Service: Web Application / API
Port: 8081
Tool Used: Nikto v2.6.0
Environment: Authorized Internal Security Assessment
Backend Identified: Apache Tomcat / Spring Boot Environment
Executive Summary

A security assessment was performed against the web application running on port 8081 to evaluate HTTP security header implementation, web server exposure, and potential information disclosure through automated web enumeration techniques.

The analysis identified that the application demonstrates partial hardening measures, including reduced server banner disclosure and restricted exposure of common CGI resources. However, multiple recommended browser security headers were not implemented, resulting in an incomplete client-side security posture.

The assessment also confirmed permissive Cross-Origin Resource Sharing (CORS) behavior and the absence of modern browser hardening policies such as Content Security Policy (CSP), Referrer Policy, and Permissions Policy.

No critical remote exploitation vectors were identified during the assessment; however, several configuration weaknesses increase exposure to browser-based attacks and reconnaissance activities.

Scope of Assessment

The following verification procedures were executed:

HTTP Security Header Verification
Web Server Enumeration
HTTP Method Validation
Cross-Origin Resource Sharing (CORS) Analysis
Resource Exposure Identification
Test 1 — HTTP Security Headers Verification
Objective

Verify whether the application exposes modern HTTP browser security headers and evaluate the default hardening posture of the web service.

Command Executed
nikto -h http://192.168.1.156:8081
Result Summary

The scan identified missing or incomplete implementation of multiple recommended HTTP security headers, including:

Content-Security-Policy (CSP)
Strict-Transport-Security (HSTS)
Referrer-Policy
Permissions-Policy
X-Content-Type-Options

Additionally, the following observations were identified:

Server banner disclosure appears minimized
HTTP OPTIONS method enabled
CORS policy configured as wildcard (*)
Deprecated clickjacking protection mechanism detected
Security Findings
[VULN-001] Missing Content-Security-Policy (CSP)
Severity

Medium

CVSS Score

5.4

Category

Security Misconfiguration

Description

The application does not implement a Content-Security-Policy (CSP) header.

Without CSP enforcement, browsers cannot properly restrict script execution sources, increasing exposure to:

Cross-Site Scripting (XSS)
Script injection attacks
Content manipulation
Third-party resource abuse
Impact

An attacker capable of injecting client-side content may execute unauthorized JavaScript within the application context.

Recommendation

Implement a restrictive CSP globally through Spring Security configuration.

Suggested Remediation
.headers(headers -> headers
    .contentSecurityPolicy(csp ->
        csp.policyDirectives("default-src 'self'")
    )
)
[VULN-002] Missing X-Content-Type-Options Header
Severity

Low

CVSS Score

3.9

Category

Security Misconfiguration

Description

The X-Content-Type-Options header was not detected.

Without the nosniff directive, browsers may attempt MIME-type sniffing, potentially interpreting malicious content incorrectly.

Impact

Attackers may exploit browser content interpretation inconsistencies to facilitate malicious content execution.

Recommendation

Enable:

X-Content-Type-Options: nosniff
Suggested Remediation
.headers(headers -> headers
    .contentTypeOptions(withDefaults())
)
[VULN-003] Permissive Cross-Origin Resource Sharing (CORS)
Severity

Medium

CVSS Score

5.3

Category

Cross-Origin Security Misconfiguration

Description

The server returned:

Access-Control-Allow-Origin: *

This configuration allows any external origin to interact with the application through browser-based requests.

Impact

An overly permissive CORS policy may expose authenticated endpoints to unintended third-party integrations or facilitate abuse scenarios in browser contexts.

Recommendation

Restrict allowed origins to trusted domains only.

Suggested Remediation
.allowedOrigins("https://trusted-domain.com")
[VULN-004] Missing Referrer-Policy Header
Severity

Low

CVSS Score

3.1

Category

Information Disclosure

Description

The application does not define a Referrer-Policy header.

Without explicit restrictions, browsers may leak internal URL structures and sensitive navigation metadata through the Referer header.

Recommendation

Implement:

Referrer-Policy: strict-origin-when-cross-origin
[VULN-005] Missing Permissions-Policy Header
Severity

Low

CVSS Score

2.9

Category

Browser Security Hardening

Description

The Permissions-Policy header was not identified.

Modern browsers use this policy to restrict access to sensitive browser capabilities such as:

Camera
Microphone
Geolocation
Sensors
Recommendation

Explicitly disable unnecessary browser permissions.

Example:

Permissions-Policy: camera=(), microphone=(), geolocation=()
Test 2 — HTTP Method Validation
Objective

Validate exposed HTTP methods and identify potentially dangerous verbs.

Result

The server responded with:

Allowed HTTP Methods: GET, HEAD, OPTIONS
Security Observation

The exposed methods appear consistent with a typical REST/web application profile.

No dangerous methods such as:

PUT
DELETE
TRACE
CONNECT

were identified during this assessment.

Test 3 — Web Resource Exposure Analysis
Objective

Identify exposed legacy paths, uncommon resources, or potentially sensitive directories.

Findings

Nikto identified a response associated with:

/js/editor/fckeditor/editor/filemanager/upload/test.html
Observation

This finding may represent:

Legacy editor artifacts
Static resource exposure
False-positive signature matching

No direct unrestricted upload capability was confirmed during the assessment.

Recommendation

Review legacy frontend assets and remove unused editor components or deprecated libraries.

Additional Observations
Server Fingerprinting Resistance

The server did not disclose a complete HTTP banner:

Server: No banner retrieved

This behavior reduces passive fingerprinting opportunities and is considered a positive hardening measure.

CGI Enumeration

Nikto did not identify accessible CGI directories:

No CGI Directories found

This indicates reduced exposure of legacy web execution interfaces.

Actions Recommended

The following remediation actions are recommended:

Implement modern HTTP security headers globally
Restrict CORS origins
Enable HTTPS and HSTS
Configure Content Security Policy (CSP)
Enable MIME-type protection headers
Remove unused frontend/editor assets
Standardize secure browser response policies
Suggested Spring Security Hardening Example
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
            .permissionsPolicy(policy ->
                policy.policy(
                    "camera=(), microphone=(), geolocation=()"
                )
            )
            .contentTypeOptions(withDefaults())
        );

    return http.build();
}
Conclusion

The application demonstrates partial security hardening and reasonable restriction of sensitive resources; however, the HTTP response hardening remains incomplete.

The absence of multiple modern browser security headers weakens the application's defensive posture against client-side attacks and information disclosure scenarios.

Although no critical vulnerabilities were identified during this assessment, the implementation of modern HTTP security controls is strongly recommended to align the application with current secure development standards and industry best practices.

Tools Used
Nikto v2.6.0
Kali Linux
Apache Tomcat Fingerprinting
HTTP Header Analysis
