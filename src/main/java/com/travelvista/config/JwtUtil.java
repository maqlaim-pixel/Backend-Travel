package com.travelvista.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // Role-based expiration: 1 hour for users, 2 hours for admins
    private static final long USER_EXPIRATION = 3600000L;    // 1 hour
    private static final long ADMIN_EXPIRATION = 7200000L;   // 2 hours

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String role, String name) {
        // Use role-based expiration
        long exp = isAdminRole(role) ? ADMIN_EXPIRATION : USER_EXPIRATION;
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("name", name)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + exp))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateToken(String email, String role, String name, long customExpiration) {
        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .claim("name", name)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + customExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    private boolean isAdminRole(String role) {
        return role != null && (role.equalsIgnoreCase("super_admin") || role.equalsIgnoreCase("admin") || role.equalsIgnoreCase("editor") || role.equalsIgnoreCase("sales") || role.equalsIgnoreCase("contributor"));
    }

    public long getExpirationMs(String role) {
        return isAdminRole(role) ? ADMIN_EXPIRATION : USER_EXPIRATION;
    }

    public String extractEmail(String token) {
        return parseClaims(token).getPayload().getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).getPayload().get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
    }
}
