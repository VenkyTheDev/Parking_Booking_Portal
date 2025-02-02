package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dto.BookingRequest;
import com.venky.parkingBookingPortal.dto.BookingResponse;
import com.venky.parkingBookingPortal.dto.RescheduleRequest;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.ForbiddenException;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import com.venky.parkingBookingPortal.exceptions.UnauthorizedException;
import com.venky.parkingBookingPortal.service.BookingService;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final UserDAO userDAO;
    private final BookingService bookingService;
    private final JwtUtil jwtUtil;
    @Autowired
    public BookingController(BookingService bookingService , JwtUtil jwtUtil , UserDAO userDAO) {
        this.bookingService = bookingService;
        this.jwtUtil = jwtUtil;
        this.userDAO = userDAO;
    }

    @PostMapping("/book")
    public ResponseEntity<String> bookParking(@RequestBody BookingRequest bookingRequest) {
        String result = bookingService.bookParking(bookingRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getBookingHistory(@PathVariable Long userId, @RequestHeader("Authorization") String token) {
        // Extract email from JWT token
        try {
            // Extract email from JWT token
            String email = jwtUtil.extractEmail(token);

            // Call the service directly with email (avoiding DAO access in the controller)
            List<BookingResponse> bookingHistory = bookingService.getBookingHistory(userId, email);

            return ResponseEntity.ok(bookingHistory);
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }

    @PostMapping("/{userId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long userId, @RequestHeader("Authorization") String token) {
        // Extract email from JWT token
        String email = jwtUtil.extractEmail(token);

        // Retrieve the requesting user from the database via service (not DAO)
        Optional<User> requestingUserOptional = userDAO.findByEmail(email);
        if (requestingUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or user not found");
        }

        User requestingUser = requestingUserOptional.get();

        // Call service method to cancel the booking
        String response = bookingService.cancelBooking(userId, requestingUser);

        if (response.contains("not found") || response.contains("permission")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reschedule")
    public ResponseEntity<?> rescheduleBooking(@RequestBody RescheduleRequest request,
                                               @RequestHeader("Authorization") String token) {
        // Call the service to handle everything
        String response = bookingService.rescheduleBooking(request, token);
        return ResponseEntity.ok(response);
    }

}
