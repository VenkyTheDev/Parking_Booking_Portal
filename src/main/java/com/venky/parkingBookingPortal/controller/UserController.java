package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dto.BookingResponse;
import com.venky.parkingBookingPortal.dto.UpdateProfileRequest;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.UnauthorizedAccessException;
import com.venky.parkingBookingPortal.service.UserService;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Log4j2
@RestController
@RequestMapping("/api/profile")
@Log
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
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
            User user = updatedUser.get();
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(user);
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
    public ResponseEntity<?> getActiveBookings(@CookieValue(name = "jwt", defaultValue = "") String token) {
        try {
            User user = userService.findUserByEmailViaToken(token);
            log.info("In the try Block");

            List<Booking> activeBookings = userService.findAllActiveBookingsList(user.getId());

            if (activeBookings.isEmpty()) {
                BookingResponse errorResponse = new BookingResponse(HttpStatus.NO_CONTENT.value(),"No active bookings found.");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(errorResponse);
            }

            List<BookingResponse> responseList = activeBookings.stream()
                    .map(BookingResponse::new) // Convert Booking to BookingResponse
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responseList);

        } catch (Exception e) {
            log.info("In the catch Block");
            BookingResponse errorResponse = new BookingResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),"An error occurred while fetching active bookings.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/add-profile-image")
    public ResponseEntity<User> addProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {
            User updatedUser = userService.uploadProfileImage(file, userId);

            return ResponseEntity.ok(updatedUser);
        } catch (IOException e) {
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @DeleteMapping("/remove-profile-image")
    public ResponseEntity<User> removeProfileImage(@RequestParam("userId") Long userId) {
        try {
            User updatedUser = userService.removeProfileImage(userId);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedUser);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}

