package com.lumora.security;

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
    private final long jwtExpiration;

    public JwtService(
            @Value("${lumora.jwt.secret}") String secret,
            @Value("${lumora.jwt.expiration-ms}") long jwtExpiration
    ) {
        try {
            byte[] keyBytes = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            if (keyBytes.length < 32) {
                throw new IllegalArgumentException(
                        "JWT secret precisa ter pelo menos 32 bytes"
                );
            }

            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
            this.jwtExpiration = jwtExpiration;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao inicializar JWT", e);
        }
    }

    // ── Gera o token JWT ─────────────────────────────────────────
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("roles", userDetails.getAuthorities()
                        .stream()
                        .map(a -> a.getAuthority())
                        .toList())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(signingKey)
                .compact();
    }

    // ── Valida o token ───────────────────────────────────────────
    public boolean isValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException e) {
            log.warn("Token inválido: {}", e.getMessage());
            return false;
        }
    }

    // ── Extrai username ──────────────────────────────────────────
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    // ── Verifica expiração ───────────────────────────────────────
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    // ── Parse do token ───────────────────────────────────────────
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpirationMs() {
        return jwtExpiration;
    }
}


