package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dto.ErrorResponse;
import com.venky.parkingBookingPortal.dto.LoginRequest;
import com.venky.parkingBookingPortal.dto.LoginResponse;
import com.venky.parkingBookingPortal.dto.SignupRequest;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Log4j2
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
        try {
            User loginResponse = authService.registerUser(request);
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        try {
            LoginResponse loginResponse = authService.authenticateUser(loginRequest, response);

            if (loginResponse != null) {
                return ResponseEntity.ok(loginResponse);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials.");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", ""); // Clear the cookie
        log.info("JWT cookie is: {}", jwtCookie.getValue());
        jwtCookie.setMaxAge(0); // Set it to expire immediately (0 seconds)
        jwtCookie.setSecure(true); // Send only over HTTPS
        jwtCookie.setHttpOnly(true); // Make it inaccessible via JavaScript
        jwtCookie.setPath("/"); // Ensure it's available across the app
        //jwtCookie.setDomain("your-domain.com"); // Uncomment and specify domain if needed
        jwtCookie.setAttribute("SameSite", "None"); // Required for cross-site cookies

        response.addCookie(jwtCookie); // Add the cookie to the response
        try{
            log.info("JWT cookie is: {}", jwtCookie.getValue());
        }catch (Exception e){
            log.info("JWT cookie is invalid");
        }

        // Return response with message
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Logout Successful");

        return ResponseEntity.ok(responseBody);
    }

}
