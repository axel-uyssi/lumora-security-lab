package com.lumora.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class JwtService {

    @Value("${lumora.jwt.secret}")
    private String secret;

    private long jwtExpiration = 3600000;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {

        byte[] keyBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        System.out.println("BYTES LENGTH: " + keyBytes.length);

        if (keyBytes.length < 32) {
            throw new RuntimeException(
                    "JWT secret precisa ter no mínimo 32 bytes"
            );
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ─────────────────────────────────────────
    // GERAR TOKEN
    // ─────────────────────────────────────────
    public String generateToken(UserDetails userDetails) {

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis() + jwtExpiration)
                )
                .signWith(signingKey)
                .compact();
    }

    // ─────────────────────────────────────────
    // VALIDAR TOKEN
    // ─────────────────────────────────────────
    public boolean isValid(
            String token,
            UserDetails userDetails
    ) {

        try {

            String username = extractUsername(token);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token);

        } catch (JwtException e) {

            log.error("Token inválido: {}", e.getMessage());

            return false;
        }
    }

    // ─────────────────────────────────────────
    // EXTRAIR USERNAME
    // ─────────────────────────────────────────
    public String extractUsername(String token) {

        return extractClaims(token).getSubject();
    }

    // ─────────────────────────────────────────
    // VERIFICAR EXPIRAÇÃO
    // ─────────────────────────────────────────
    private boolean isTokenExpired(String token) {

        return extractClaims(token)
                .getExpiration()
                .before(new Date());
    }

    // ─────────────────────────────────────────
    // EXTRAIR CLAIMS
    // ─────────────────────────────────────────
    private Claims extractClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}



