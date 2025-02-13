package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dto.BookingResponse;
import com.venky.parkingBookingPortal.dto.UpdateProfileRequest;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.UnauthorizedAccessException;
import com.venky.parkingBookingPortal.service.UserService;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
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
//            return ResponseEntity.ok("Profile updated successfully!");
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

//    @GetMapping("/activeBookings")
//    public ResponseEntity<?> getActiveBookings(@RequestHeader("Authorization") String token) {
//        try{
//            User user = userService.findUserByEmailViaToken(token);
//
//            List<Booking> activeBookings = userService.findAllActiveBookingsList(user.getId());
//
//            if (activeBookings.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No active bookings found.");
//            }
//
//            List<BookingResponse> responseList = activeBookings.stream()
//                    .map(BookingResponse::new) // Convert Booking to BookingResponse
//                    .collect(Collectors.toList());
//            return ResponseEntity.ok(responseList);
//        }catch(Exception e){
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }

    @GetMapping("/activeBookings")
    public ResponseEntity<?> getActiveBookings(@CookieValue(name = "jwt", defaultValue = "") String token) {
        try {
            // Retrieve the user from the token
            User user = userService.findUserByEmailViaToken(token);
            log.info("In the try Block");

            // Fetch the active bookings for the user
            List<Booking> activeBookings = userService.findAllActiveBookingsList(user.getId());

            // If no active bookings are found, return a No Content status with a custom message
            if (activeBookings.isEmpty()) {
                BookingResponse errorResponse = new BookingResponse(HttpStatus.NO_CONTENT.value(),"No active bookings found.");
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(errorResponse);
            }

            // Map the list of Booking entities to BookingResponse DTOs
            List<BookingResponse> responseList = activeBookings.stream()
                    .map(BookingResponse::new) // Convert Booking to BookingResponse
                    .collect(Collectors.toList());

            // Return the list of BookingResponse objects with an OK status
            return ResponseEntity.ok(responseList);

        } catch (Exception e) {
            log.info("In the catch Block");
            // In case of an internal error, return a generic error message with a 500 status code
            BookingResponse errorResponse = new BookingResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),"An error occurred while fetching active bookings.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/add-profile-image")
    public ResponseEntity<User> addProfileImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        try {
            // Upload the profile image using the service and get the updated User object
            User updatedUser = userService.uploadProfileImage(file, userId);

            // Return the updated User in the response with HTTP 200 OK
            return ResponseEntity.ok(updatedUser);
        } catch (IOException e) {
            e.printStackTrace();

            // Return error response with INTERNAL_SERVER_ERROR status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @DeleteMapping("/remove-profile-image")
    public ResponseEntity<User> removeProfileImage(@RequestParam("userId") Long userId) {
        try {
            // Call the service to remove the profile image and get the updated User object
            User updatedUser = userService.removeProfileImage(userId);

            // Return the updated User in the response with HTTP 200 OK
//            return ResponseEntity.ok(updatedUser);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(updatedUser);
        } catch (Exception e) {
            e.printStackTrace();

            // Return error response with INTERNAL_SERVER_ERROR status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}

