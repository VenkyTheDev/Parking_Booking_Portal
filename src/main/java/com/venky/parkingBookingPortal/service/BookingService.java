package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.BookingDAO;
import com.venky.parkingBookingPortal.dao.ParkingDAO;
import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dto.BookingRequest;
import com.venky.parkingBookingPortal.dto.BookingResponse;
import com.venky.parkingBookingPortal.dto.RescheduleRequest;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.Parking;
import com.venky.parkingBookingPortal.entity.Role;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.ForbiddenException;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;
    private final ParkingDAO parkingDAO;
    private final JwtUtil jwtUtil;

    @Autowired
    public BookingService(BookingDAO bookingDAO, ParkingDAO parkingDAO, UserDAO userDAO, JwtUtil jwtUtil) {
        this.bookingDAO = bookingDAO;
        this.parkingDAO = parkingDAO;
        this.userDAO = userDAO;
        this.jwtUtil = jwtUtil;
    }

//    public String bookParking(BookingRequest request) {
//        // Retrieve user by userId
//        Optional<User> userOptional = userDAO.findById(request.getUserId());
//        if (userOptional.isEmpty()) {
//            return "User not found!";
//        }
//
//        // Retrieve parking by parkingId
//        Optional<Parking> parkingOptional = parkingDAO.findById(request.getParkingId());
//        if (parkingOptional.isEmpty()) {
//            return "Parking lot not found!";
//        }
//
//        Parking parking = parkingOptional.get();
//
//        // Check if there are available slots
//        if (parking.getTotalSlots() <= 0) {
//            return "No available slots!";
//        }
//
//        // Create a new booking
//        Booking booking = new Booking();
//        booking.setUser(userOptional.get());
//        booking.setParking(parking);
//        booking.setStartTime(request.getStartTime());
//        booking.setEndTime(request.getEndTime());
//        booking.setStatus(Booking.Status.SUCCESS);
//
//        // Save the booking
//        bookingDAO.save(booking);
//
//        // Decrement the available slots in parking
//        parking.decrementSlots();
//        parkingDAO.save(parking);
//
//        return "Booking successful!";
//    }

    public String bookParking(BookingRequest request) {
        // Retrieve user by userId
        Optional<User> userOptional = userDAO.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return "User not found!";
        }

        User user = userOptional.get();

        // Retrieve parking by parkingId
        Optional<Parking> parkingOptional = parkingDAO.findById(request.getParkingId());
        if (parkingOptional.isEmpty()) {
            return "Parking lot not found!";
        }

        Parking parking = parkingOptional.get();

        // Check if there are available slots
        if (parking.getTotalSlots() <= 0) {
            return "No available slots!";
        }

        LocalDateTime now = LocalDateTime.now();
        if (user.getRole() != Role.ADMIN && request.getStartTime().isAfter(now)) {
            return "Pre-booking is not allowed!";
        }

        // Check if the user already has an active booking (Admins can book multiple slots)
        if (user.getRole() != Role.ADMIN) {
            Optional<Booking> latestBooking = bookingDAO.findFirstByUserIdOrderByStartTimeDesc(user.getId());

            if (latestBooking.isPresent()) {
                Booking lastBooking = latestBooking.get();

                // If the last booking is CANCELLED or doesn't overlap with the requested time, allow booking
                if (lastBooking.getStatus() == Booking.Status.CANCELLED || lastBooking.getEndTime().isBefore(request.getStartTime())) {
                    // Proceed with the booking
                } else {
                    return "You already have an active booking overlapping with the requested time!";
                }
            }
        }


        // Create a new booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setParking(parking);
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setStatus(Booking.Status.SUCCESS);

        // Save the booking
        bookingDAO.save(booking);

        // Decrement the available slots in parking
        parking.decrementSlots();
        parkingDAO.save(parking);

        return "Booking successful!";
    }

    public List<BookingResponse> getBookingHistory(Long userId, String email) {
        // Fetch the user from the database using email (from token)
        Optional<User> currentUserOptional = userDAO.findByEmail(email);
        if (currentUserOptional.isEmpty()) {
            throw new NotFoundException("User not found!");
        }

        User currentUser = currentUserOptional.get();

        // Allow access if the user is an admin OR if the user is requesting their own history
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to access this user's booking history!");
        }

        // Fetch bookings based on role
        List<Booking> bookings;
        if (currentUser.getRole() == Role.ADMIN) {
            if(currentUser.getId().equals(userId)){
                bookings = bookingDAO.findAll();
            }
            else {
                bookings = bookingDAO.findByUserId(userId);
                // Admins get all booking history
            }
        } else {
            bookings = bookingDAO.findByUserId(userId); // Normal users get their own history
        }

        return bookings.stream()
                .map(BookingResponse::new)
                .collect(Collectors.toList());
    }


    public String cancelBooking(Long userId, User requestingUser) {
        // Retrieve the latest active booking for the given user ID
        Optional<Booking> bookingOptional = bookingDAO.findFirstByUserIdAndStatusOrderByStartTimeDesc(userId, Booking.Status.SUCCESS);

        if (bookingOptional.isEmpty()) {
            return "No active booking found for this user!";
        }

        Booking booking = bookingOptional.get();

        // Check if the requesting user is an admin or the owner of the booking
        if (requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(userId)) {
            return "You do not have permission to cancel this booking!";
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(booking.getStartTime()) ||
                (now.isAfter(booking.getStartTime()) && now.isBefore(booking.getEndTime()))) {

            // Check if the booking is already cancelled
            if (booking.getStatus() == Booking.Status.CANCELLED) {
                return "Booking is already cancelled!";
            }

            // Update the status to CANCELLED
            booking.setStatus(Booking.Status.CANCELLED);
            bookingDAO.save(booking);

            // Increment the available slots in the parking lot
            Parking parking = booking.getParking();
            parking.incrementSlots();
            parkingDAO.save(parking);

            return "Booking cancelled successfully!";
        } else if (now.isAfter(booking.getEndTime())) {
            return "Your slot has ended, cancellation is not possible.";
        }

        return "Unknown error";
    }

    public String rescheduleBooking(RescheduleRequest request, String token) {
        // Extract email from JWT token
        String email = jwtUtil.extractEmail(token);

        // Retrieve the requesting user from the database
        Optional<User> requestingUserOptional = userDAO.findByEmail(email);
        if (requestingUserOptional.isEmpty()) {
            return "Invalid token or user not found";
        }

        User requestingUser = requestingUserOptional.get();
        Long bookingId = request.getBookingId();
        Long userId = request.getUserId();
        LocalDateTime newStartTime = request.getNewStartTime();
        LocalDateTime newEndTime = request.getNewEndTime();

        // Retrieve the user by ID
        Optional<User> userOptional = userDAO.findById(userId);
        if (userOptional.isEmpty()) {
            return "User not found!";
        }
        User user = userOptional.get();

        // Retrieve booking by ID
        Optional<Booking> bookingOptional = bookingDAO.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            return "Booking not found!";
        }
        Booking booking = bookingOptional.get();

        // Check if the requesting user is an admin or the owner of the booking
        if (requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(userId)) {
            return "You do not have permission to reschedule this booking!";
        }

        // Check if the booking belongs to the given user ID
        if (!booking.getUser().getId().equals(userId)) {
            return "Booking does not belong to the provided user ID!";
        }

        // Retrieve parking details
        Parking parking = booking.getParking();

        // Count overlapping bookings for the requested time
        long overlappingBookings = getOverlappingBookingCount(parking.getId(), newStartTime, newEndTime);

        // Check if the user's current booking overlaps with the new requested time
        boolean isUserBookingOverlapping = doesBookingOverlap(booking, newStartTime, newEndTime);

        // If the user's current booking is counted in overlapping bookings, remove it
        if (isUserBookingOverlapping) {
            overlappingBookings -= 1;
        }

        // Check if another booking is ending before the new start time (freeing up a slot)
        boolean isSlotFreeingUp = isAnyBookingEndingBefore(parking.getId(), newStartTime);

        // Allow rescheduling if slots are available OR a booking is freeing up
        if (overlappingBookings >= parking.getTotalSlots() && !isSlotFreeingUp) {
            return "No available slot for the requested time!";
        }

        // Update booking time
        booking.setStartTime(newStartTime);
        booking.setEndTime(newEndTime);
        bookingDAO.save(booking);

        return "Booking rescheduled successfully!";
    }

    // Helper method to check if a booking overlaps with the new time
    private boolean doesBookingOverlap(Booking booking, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        return !(booking.getEndTime().isBefore(newStartTime) || booking.getStartTime().isAfter(newEndTime));
    }

    // Helper method to check if any booking ends before the new start time, freeing up a slot
    private boolean isAnyBookingEndingBefore(Long parkingId, LocalDateTime newStartTime) {
        return bookingDAO.existsByParkingIdAndEndTimeBefore(parkingId, newStartTime);
    }

    private long getOverlappingBookingCount(Long parkingId, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        return bookingDAO.countByParkingAndTimeRange(parkingId, newStartTime, newEndTime);
    }

}
