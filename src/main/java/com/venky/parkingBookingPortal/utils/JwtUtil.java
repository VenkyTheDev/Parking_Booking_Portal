package com.venky.parkingBookingPortal.utils;

import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {


    private String secretkey = "^%fyf454@,l./?feihfur3gfuy7y345y7";
//    public String generateToken(String email) {
//
//        SecretKey key = Keys.hmacShaKeyFor(secretkey.getBytes(StandardCharsets.UTF_8));
//
//        return Jwts.builder().
//                subject(email).
//                issuedAt(new Date()).
//                expiration(new Date(System.currentTimeMillis() + 86400000)).
//                signWith(key).
//                compact();
//    }

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
