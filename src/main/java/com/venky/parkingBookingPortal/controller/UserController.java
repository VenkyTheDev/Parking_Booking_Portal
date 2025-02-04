package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dto.BookingResponse;
import com.venky.parkingBookingPortal.dto.UpdateProfileRequest;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.UnauthorizedAccessException;
import com.venky.parkingBookingPortal.service.UserService;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/profile")
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
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

    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable Long userId, @RequestHeader("Authorization") String token) {
        // Call the service to get the profile data
        try {
            User user = userService.findUserByEmailViaToken(token);
            User profile = userService.getProfile(userId, user);
            return ResponseEntity.ok(profile);
        } catch (UnauthorizedAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching the profile.");
        }
    }

    @GetMapping("/activeBookings")
    public ResponseEntity<?> getActiveBookings(@RequestHeader("Authorization") String token) {
        try{
            User user = userService.findUserByEmailViaToken(token);

            List<Booking> activeBookings = userService.findAllActiveBookingsList(user.getId());

            if (activeBookings.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No active bookings found.");
            }

            List<BookingResponse> responseList = activeBookings.stream()
                    .map(BookingResponse::new) // Convert Booking to BookingResponse
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responseList);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}

