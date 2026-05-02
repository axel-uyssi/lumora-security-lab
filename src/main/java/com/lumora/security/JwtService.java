package com.lumora.security;

// ─────────────────────────────────────────────────────────────────────────────
// CORREÇÃO: jjwt 0.12+ mudou a API completamente
//
// ANTES (jjwt 0.9 — ANTIGO, causava os erros):
//   Jwts.parserBuilder()          → não existe mais
//   .setSigningKey(key)           → deprecated
//   .setSubject(email)            → deprecated
//   .setIssuedAt(date)            → deprecated
//   .setExpiration(date)          → deprecated
//   .signWith(key, algorithm)     → deprecated
//   .parseClaimsJws(token)        → deprecated
//
// DEPOIS (jjwt 0.12 — CORRETO):
//   Jwts.parser()                 → novo método
//   .verifyWith(key)              → novo
//   .subject(email)               → novo
//   .issuedAt(date)               → novo
//   .expiration(date)             → novo
//   .signWith(key)                → novo (detecta algoritmo automaticamente)
//   .parseSignedClaims(token)     → novo
//   .getPayload()                 → novo
// ─────────────────────────────────────────────────────────────────────────────

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    private final SecretKey signingKey;
    private final long      jwtExpiration;

    public JwtService(
            @Value("${lumora.jwt.secret}") String secret,
            @Value("${lumora.jwt.expiration-ms}") long jwtExpiration
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.jwtExpiration = jwtExpiration;
    }


    // ── Gera o token JWT ──────────────────────────────────────────────────────
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())              // "sub" = email
                .claim("roles", userDetails.getAuthorities()     // roles no payload
                        .stream()
                        .map(a -> a.getAuthority())
                        .toList())
                .issuedAt(new Date())                            // "iat" = emitido em
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // "exp"
                .signWith(signingKey)                            // assina com HMAC-SHA256
                .compact();                                      // serializa para String
    }

    // ── Valida o token ────────────────────────────────────────────────────────
    public boolean isValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    // ── Extrai o email do token ───────────────────────────────────────────────
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ── Verifica expiração ────────────────────────────────────────────────────
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // ── Parse completo do token ───────────────────────────────────────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)          // verifica assinatura
                .build()
                .parseSignedClaims(token)        // faz o parse
                .getPayload();                   // retorna o payload (Claims)
    }

    // ── Retorna tempo de expiração ────────────────────────────────────────────
    public long getExpirationMs() {
        return jwtExpiration;
    }
}



