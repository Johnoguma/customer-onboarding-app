package com.backend.app.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.expiration}")
    private long expirationMillis;

    @Value("${jwt.refreshExpiration:86400000}")
    private long refreshExpirationMillis;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512); // Generates a strong 512-bit key
        System.out.println("[JwtUtil] Initialized with a secure dynamic key");
    }

    private SecretKey getSigningKey() {
        return this.key;
    }

    public String generateToken(String email) {
        return buildToken(email, expirationMillis);
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, refreshExpirationMillis);
    }

    private String buildToken(String subject, long validityMillis) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + validityMillis);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public String extractSubject(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public String extractUserId(String token) {
        return extractSubject(token);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public long getExpiration() {
        return expirationMillis;
    }

    public long getRefreshExpiration() {
        return refreshExpirationMillis;
    }
}
