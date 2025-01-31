package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dto.UpdateProfileRequest;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId, @Valid @RequestBody UpdateProfileRequest request) {
        Optional<User> updatedUser = userService.updateUser(userId, request);

        if (updatedUser.isPresent()) {
            return ResponseEntity.ok("Profile updated successfully!");
        } else {
            return ResponseEntity.badRequest().body("User not found or update failed.");
        }
    }
}
