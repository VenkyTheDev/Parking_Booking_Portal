package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dto.ErrorResponse;
import com.venky.parkingBookingPortal.dto.LoginRequest;
import com.venky.parkingBookingPortal.dto.LoginResponse;
import com.venky.parkingBookingPortal.dto.SignupRequest;
import com.venky.parkingBookingPortal.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try{
            String token = authService.registerUser(request);
            return ResponseEntity.ok(new LoginResponse(token, "User registered successfully!"));
        } catch (Exception e){
            return ResponseEntity.ok(new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            String token = authService.authenticateUser(loginRequest);
            if (token != null) {
                return ResponseEntity.ok(new LoginResponse(token, "Login successful!"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", ""); // Clear the cookie
        jwtCookie.setMaxAge(1); // Set it to expire immediately
        jwtCookie.setSecure(true); // Send only over HTTPS
        jwtCookie.setHttpOnly(true); // Make it inaccessible via JavaScript
        jwtCookie.setPath("/"); // Ensure it's available across the app
        //jwtCookie.setDomain("domain.com"); // Set your domain if needed
        jwtCookie.setAttribute("SameSite", "None"); // Required for cross-site cookies

        response.addCookie(jwtCookie); // Add the cookie to the response

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Logout Successful");

        return ResponseEntity.ok(responseBody);
    }

}
