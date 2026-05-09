Security Assessment Report
HTTP Security Headers Verification
Test Information
Date: 2026-05-03
Target Host: 192.168.1.19
Service: Web Application / API
Port: 8081
Tool Used: Nmap and ffuf
Environment: Authorized internal security assessment
Executive Summary

A security assessment was conducted against the web application running on port 8081 to evaluate the presence of recommended HTTP security headers and identify potential exposure of hidden resources through directory enumeration.

The analysis identified that the application responds with multiple 403 Forbidden responses for hidden or sensitive paths, indicating that directory access restrictions are active. However, the application does not properly expose several recommended HTTP security headers by default, increasing the risk of client-side attacks such as clickjacking, MIME-type confusion, and content injection.

The target appears to be a Spring Boot application with default or incomplete hardening configuration.

Scope of Assessment

The following tests were executed:

HTTP Security Headers Verification
Directory Enumeration / Fuzzing
Exposure Validation of Restricted Paths
Test 1 — HTTP Security Headers Verification
Objective

Verify whether the application exposes recommended HTTP security headers by default.

Command Executed
nmap -p 8081 --script http-security-headers 192.168.1.19
Result

The scan indicated missing or incomplete implementation of recommended security headers, including:

X-Frame-Options
Content-Security-Policy
X-Content-Type-Options
Referrer-Policy
Risk Analysis
Missing X-Frame-Options

Without the X-Frame-Options header, the application may be vulnerable to clickjacking attacks, allowing malicious websites to embed the application inside invisible frames and trick users into performing unintended actions.

Missing Content-Security-Policy (CSP)

The absence of a CSP increases exposure to:

Cross-Site Scripting (XSS)
Injection of unauthorized scripts
Content manipulation attacks
Missing X-Content-Type-Options

Without nosniff, browsers may incorrectly interpret content types, potentially enabling malicious file execution.

Test 2 — Directory Enumeration
Objective

Identify hidden directories, administrative paths, or sensitive resources exposed by the application.

Command Executed
ffuf -w /usr/share/wordlists/dirb/common.txt -u http://172.31.160.1:8081/FUZZ
Result Summary

The enumeration returned multiple 403 Forbidden responses for sensitive or hidden paths, including:

/admin
/.git
/.svn
/.htaccess
/_archive
/_cache
/.mysql_history

The server blocks direct access to these resources; however, their existence may still provide valuable reconnaissance information to an attacker.

Security Observation

Although access was denied, the exposure of valid resource paths may assist attackers during:

Reconnaissance
Enumeration
Target profiling
Chained exploitation attempts
Vulnerabilities Identified
[VULN-001] Missing Security Headers
Severity: Medium
CVSS Score: 5.3
Category: Security Misconfiguration
OWASP Reference: A05:2021 – Security Misconfiguration
Description

The application does not implement essential HTTP response headers responsible for browser-side protection mechanisms.

Impact

An attacker may leverage missing protections to:

Perform clickjacking attacks
Facilitate XSS exploitation
Manipulate browser content interpretation
Recommendation

Implement secure headers globally through the Spring Security configuration.

Suggested Remediation (Spring Boot)
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
http
.headers(headers -> headers
.frameOptions(frame -> frame.deny())
.contentSecurityPolicy(csp ->
csp.policyDirectives("default-src 'self'"))
.xssProtection(xss -> xss.disable())
.contentTypeOptions(withDefaults())
);

    return http.build();
}
[VULN-002] Directory Enumeration Disclosure
Severity: Low
CVSS Score: 3.7
Category: Information Disclosure
Description

The server reveals the existence of sensitive directories and internal paths through HTTP response behavior during fuzzing.

Impact

Although access is restricted, attackers may use this information for:

Attack surface mapping
Targeted exploitation attempts
Repository exposure verification
Recommendation
Disable unnecessary routes and hidden resources
Configure uniform error handling
Consider returning generic responses for non-existing resources
Actions Taken

The missing HTTP security headers were identified as a configuration weakness in the default Spring Boot setup.

The following remediation actions were recommended:

Add browser security headers using SecurityFilterChain
Enforce a restrictive Content-Security-Policy
Enable X-Frame-Options: DENY
Implement additional hardening headers such as:
Strict-Transport-Security
Referrer-Policy
Permissions-Policy

At the time of this assessment:

Directory access restrictions were already functioning correctly (403 Forbidden)
No direct unauthorized access to sensitive resources was observed
Header hardening remained pending implementation
Conclusion

The application demonstrates basic access restriction mechanisms; however, the HTTP response hardening is incomplete and does not meet modern secure configuration standards.

While no critical exploitation vectors were identified during this assessment, the absence of recommended security headers increases exposure to browser-based attacks and weakens the application's defensive posture.

Immediate implementation of secure HTTP headers is recommended as part of the application hardening process.

Tools Used
Nmap
ffuf
Kali Linux