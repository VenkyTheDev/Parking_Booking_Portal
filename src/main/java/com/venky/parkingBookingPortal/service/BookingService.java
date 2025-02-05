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
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final double EARTH_RADIUS_KM = 6371;
    private final BookingDAO bookingDAO;
    private final UserDAO userDAO;
    private final ParkingDAO parkingDAO;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final ParkingService parkingService;

    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired
    public BookingService(BookingDAO bookingDAO, ParkingDAO parkingDAO, UserDAO userDAO, JwtUtil jwtUtil, UserService userService, ParkingService parkingService) {
        this.bookingDAO = bookingDAO;
        this.parkingDAO = parkingDAO;
        this.userDAO = userDAO;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.parkingService = parkingService;
    }

    public String bookParking(BookingRequest request) {
        // Retrieve user by userId
        Optional<User> userOptional = userDAO.findById(request.getUserId());
        if (userOptional.isEmpty()) {
            return "User not found!";
        }

        User user = userOptional.get();

        //Checking for the flag
        Long allowedAfter = user.getAllowedAfter();
        long nowTime = Instant.now().toEpochMilli();
        if(allowedAfter != null && allowedAfter > nowTime){
            return "You are not allowed to book parking! till " + allowedAfter + " milliseconds";
        }
        LocalDateTime now = LocalDateTime.now().plusMinutes(1);
        // Retrieve parking by parkingId
        Optional<Parking> parkingOptional = parkingDAO.findById(request.getParkingId());
        if (parkingOptional.isEmpty()) {
            return "Parking lot not found!";
        }

        Parking parking = parkingOptional.get();

        if (user.getRole() != Role.ADMIN && request.getStartTime().isAfter(now)) {
            return "Pre-booking is not allowed!";
        }
        //Checking Distance
        Point parkingLocation = parking.getLocation();
        try{
            Point userLocation = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
            if((calculateDistance(parkingLocation , userLocation) > 100) && user.getRole() != Role.ADMIN){
                return "Please come closer to the parking location";
            }
        }catch (Exception e){
            throw new IllegalArgumentException();
        }

        // Check if there are available slots
        int availableSlots = parkingService.fetchingAvailableSlots(request.getParkingId() , request.getStartTime() ,request.getEndTime());
        if (availableSlots <= 0) {
            return "No available slots!";
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
//        parking.decrementSlots();
        parkingDAO.save(parking);

        return "Booking successful!";
    }

    public List<BookingResponse> getBookingHistory(Long userId, User currentUser) {
        // Fetch the user from the database using email (from token)

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
        User requestingUser = userService.findUserByEmailViaToken(token);

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

        // Update booking time
        if(newStartTime != null){
            booking.setStartTime(newStartTime);
        }
        if(newEndTime != null) {
            booking.setEndTime(newEndTime);
        }
        bookingDAO.save(booking);

        return "Booking rescheduled successfully!";
    }

    public List<Booking> getBookedSlots(String token){
        User user = userService.findUserByEmailViaToken(token);
        if (user == null) {throw new NotFoundException("User not found!");
        }
        List<Booking> bookings;
        bookings = bookingDAO.findAllActiveBookings();
        return bookings;
    }

    public double calculateDistance(Point point1, Point point2) {
        // Check if both points are non-null
        if (point1 == null || point2 == null) {
            throw new IllegalArgumentException("Both points must be non-null");
        }

        // Extract the latitude and longitude of the points
        double lat1 = point1.getY();
        double lon1 = point1.getX();
        double lat2 = point2.getY();
        double lon2 = point2.getX();

        // Convert degrees to radians
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate the distance in kilometers
        double distanceInKilometers = EARTH_RADIUS_KM * c;

        // Convert the distance to meters
        return distanceInKilometers * 1000; // Distance in meters
    }

}
