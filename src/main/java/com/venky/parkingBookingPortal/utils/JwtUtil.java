package com.venky.parkingBookingPortal.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${secretkey}")
    private String secretkey;

    public String generateToken(String email) {
        SecretKey key = Keys.hmacShaKeyFor(secretkey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretkey.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(key) // Verify the token with the key
                .build()
                .parseSignedClaims(token.replace("Bearer ", "")) // Remove "Bearer " prefix and parse
                .getPayload(); // Get the claims
    }

    // Extract the email from the token
    public String extractEmail(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject(); // Get the subject (email)
    }
}
