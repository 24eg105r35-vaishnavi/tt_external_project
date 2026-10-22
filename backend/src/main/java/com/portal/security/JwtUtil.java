package com.portal.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${app.jwt.secret:studentachievementportaljwtsecretkeymustbelongenough256bits}") String secret,
                   @Value("${app.jwt.expiration-ms:7200000}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // generate JWT token with email subject and role claim
    public String generateToken(String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    // extract email subject from token
    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.getSubject() : null;
    }

    // extract role claim from token
    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    // check if token is expired
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = extractAllClaims(token);
            if (claims == null || claims.getExpiration() == null) {
                return true;
            }
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    // validate token against user details
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        if (username == null || userDetails == null) {
            return false;
        }
        return (username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // parse and extract claims from JWT
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }
}
