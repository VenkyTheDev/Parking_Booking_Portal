package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.ForbiddenException;
import com.venky.parkingBookingPortal.exceptions.UnauthorizedException;
import com.venky.parkingBookingPortal.service.UserService;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AdminController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId,
                                        @RequestHeader("Authorization") String token) {
        try {
            // Call the service layer to handle the user deletion logic
            boolean isDeleted = userService.deleteUserByAdmin(userId, token);

            if (isDeleted) {
                return ResponseEntity.ok("User deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/flag/{userId}")
    public ResponseEntity<String> flagUser(@PathVariable Long userId, @RequestBody int days) {
        String result = userService.flagUser(userId, days);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/unflag/{userId}")
    public ResponseEntity<String> unflagUser(@PathVariable Long userId) {
        String result = userService.unflagUser(userId);
        return ResponseEntity.ok(result);
    }
}
