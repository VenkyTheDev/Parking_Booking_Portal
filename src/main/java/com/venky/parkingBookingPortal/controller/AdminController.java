package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        boolean isDeleted = userService.deleteUser(userId);
        if (isDeleted) {
            return ResponseEntity.ok("User deleted successfully.");
        } else {
            return ResponseEntity.status(404).body("User not found.");
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
