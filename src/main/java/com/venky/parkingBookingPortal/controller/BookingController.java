package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dto.BookingRequest;
import com.venky.parkingBookingPortal.dto.BookingResponse;
import com.venky.parkingBookingPortal.dto.RescheduleRequest;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.ForbiddenException;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import com.venky.parkingBookingPortal.exceptions.UnauthorizedException;
import com.venky.parkingBookingPortal.service.BookingService;
import com.venky.parkingBookingPortal.service.ParkingService;
import com.venky.parkingBookingPortal.service.UserService;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final UserDAO userDAO;
    private final BookingService bookingService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ParkingService parkingService;

    @Autowired
    public BookingController(BookingService bookingService , JwtUtil jwtUtil , UserDAO userDAO, UserService userService, ParkingService parkingService) {
        this.bookingService = bookingService;
        this.jwtUtil = jwtUtil;
        this.userDAO = userDAO;
        this.userService = userService;
        this.parkingService = parkingService;
    }

    @PostMapping("/book")
    public ResponseEntity<BookingResponse> bookParking(@RequestBody BookingRequest bookingRequest) {
        // Call the service method to handle the booking
        BookingResponse bookingResponse = bookingService.bookParking(bookingRequest);

        if (bookingResponse.getStatusCode() == HttpStatus.NOT_FOUND.value()) {
            log.info("Booking not found");
            return ResponseEntity.ok(bookingResponse);
        } else if (bookingResponse.getStatusCode() == HttpStatus.FORBIDDEN.value()) {
            log.info("Forbidden");
            return ResponseEntity.ok(bookingResponse);
        } else if (bookingResponse.getStatusCode() == HttpStatus.BAD_REQUEST.value()) {
            log.info("Bad request");
            return ResponseEntity.ok(bookingResponse);
        } else {
            log.info("Booking Succesfull" , bookingResponse);
            return ResponseEntity.ok(bookingResponse);
        }
    }


    @GetMapping("/history/{userId}")
    public ResponseEntity<?> getBookingHistory(@PathVariable Long userId, @RequestHeader("Authorization") String token) {
        try {

            User user = userService.findUserByEmailViaToken(token);
            List<BookingResponse> bookingHistory = bookingService.getBookingHistory(userId , user);

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


    @GetMapping("/allhistory")
    public ResponseEntity<Map<String, Object>> getBookingHistory(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        User user = userService.findUserByEmailViaCookie(request);
        if (user == null) {
            return ResponseEntity.status(401).body(null);
        }

        List<Booking> bookings = bookingService.getAllBookingHistory(user, page, size);
        long totalBookings = bookingService.getTotalBookingCount(user);

        Map<String, Object> response = new HashMap<>();
        response.put("bookings", bookings);
        response.put("totalBookings", totalBookings);
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }



    @PostMapping("/cancel/{userId}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long userId, HttpServletRequest token , @RequestBody Long bookingId) {
        User requestingUser = userService.findUserByEmailViaCookie(token);

        log.info("User " + requestingUser);

        // Call service method to cancel the booking
        String response = bookingService.cancelBooking(userId, requestingUser , bookingId);

        if (response.contains("not found") || response.contains("permission")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reschedule")
    public ResponseEntity<?> rescheduleBooking(@RequestBody RescheduleRequest request,
                                               HttpServletRequest token) {
        // Call the service to handle everything
        String response = bookingService.rescheduleBooking(request, token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/bookedSlots")
    public ResponseEntity<?> getBookedSlots(@RequestHeader("Authorization") String token) {
        List<Booking> bookings = bookingService.getBookedSlots(token);
        return ResponseEntity.ok(bookings);
    }


}
