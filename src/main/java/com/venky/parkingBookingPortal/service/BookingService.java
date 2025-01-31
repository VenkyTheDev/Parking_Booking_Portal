package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.BookingDAO;
import com.venky.parkingBookingPortal.dao.ParkingDAO;
import com.venky.parkingBookingPortal.dao.ParkingDAOJpaImpl;
import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dto.BookingRequest;
import com.venky.parkingBookingPortal.dto.BookingResponse;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.Parking;
import com.venky.parkingBookingPortal.entity.Role;
import com.venky.parkingBookingPortal.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;
    private final ParkingDAO parkingDAO;

    @Autowired
    public BookingService(BookingDAO bookingDAO, ParkingDAO parkingDAO, UserDAO userDAO) {
        this.bookingDAO = bookingDAO;
        this.parkingDAO = parkingDAO;
        this.userDAO = userDAO;
    }

    public String bookParking(BookingRequest request) {
        // Retrieve user by userId
        Optional<User> userOptional = userDAO.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return "User not found!";
        }

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

        // Create a new booking
        Booking booking = new Booking();
        booking.setUser(userOptional.get());
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

    public List<BookingResponse> getBookingHistory(Long userId) {
        // Check if user exists
        Optional<User> userOptional = userDAO.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found!");
        }

        User user = userOptional.get();

        List<Booking> bookings;
        if (user.getRole() == Role.ADMIN) { // If user is ADMIN, fetch all bookings
            bookings = bookingDAO.findAll();
        } else { // Otherwise, fetch only the user's bookings
            bookings = bookingDAO.findByUserId(userId);
        }

        return bookings.stream()
                .map(BookingResponse::new)
                .collect(Collectors.toList());
    }

    public String cancelBooking(Long bookingId, User requestingUser) {
        // Retrieve the booking by ID
        Optional<Booking> bookingOptional = bookingDAO.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            return "Booking not found!";
        }

        Booking booking = bookingOptional.get();
        User bookedUser = booking.getUser();

        // Check if the requesting user is an admin or the owner of the booking
        if (requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(bookedUser.getId())) {
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

}
