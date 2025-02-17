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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

public BookingResponse bookParking(BookingRequest request) {
    // Retrieve user by userId
    Optional<User> userOptional = userDAO.findById(request.getUserId());
    if (userOptional.isEmpty()) {
        // Return BookingResponse with error message
        return new BookingResponse(HttpStatus.NOT_FOUND.value(),"User not found!");
    }

    User user = userOptional.get();

    // Checking for the flag
    Long allowedAfter = user.getAllowedAfter();
    long nowTime = Instant.now().toEpochMilli();
    if (allowedAfter != null && allowedAfter > nowTime) {
        return new BookingResponse(HttpStatus.FORBIDDEN.value(),"You are not allowed to book parking! Till " + allowedAfter + " milliseconds");
    }

    LocalDateTime now = LocalDateTime.now().plusMinutes(1);

    // Retrieve parking by parkingId
    Optional<Parking> parkingOptional = parkingDAO.findById(request.getParkingId());
    if (parkingOptional.isEmpty()) {
        return new BookingResponse(HttpStatus.NOT_FOUND.value(),"Parking lot not found!");
    }

    Parking parking = parkingOptional.get();

    if (user.getRole() != Role.ADMIN && request.getStartTime().isAfter(now)) {
        return new BookingResponse(HttpStatus.BAD_REQUEST.value(),"Pre-booking is not allowed!");
    }

    if (request.getEndTime().toLocalTime().isAfter(LocalTime.of(18, 30)) && user.getRole() != Role.ADMIN) {
        return new BookingResponse(HttpStatus.BAD_REQUEST.value(),"Booking after 6:30 P.M is not allowed");
    }

    // Checking Distance
    if (parking.getLocation() == null) {
        throw new ForbiddenException("Parking location is null!");
    }

    if (user.getRole() != Role.ADMIN) {
        Optional<Booking> latestBooking = bookingDAO.findFirstByUserIdOrderByStartTimeDesc(user.getId());

        if (latestBooking.isPresent()) {
            Booking lastBooking = latestBooking.get();

            if (lastBooking.getStatus() == Booking.Status.CANCELLED || lastBooking.getEndTime().isBefore(request.getStartTime())) {
            } else {
                return new BookingResponse(HttpStatus.BAD_REQUEST.value(),"You already have an active booking!");
            }
        }
    }

    Point parkingLocation = parking.getLocation();
    try {
        Point userLocation = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
        if (userLocation.isEmpty()) {
            return new BookingResponse(HttpStatus.FORBIDDEN.value(),"Parking location is empty!");
        }
//        if ((calculateDistance(parkingLocation, userLocation) > 100)    ) {
//            return new BookingResponse(HttpStatus.FORBIDDEN.value(),"Please come closer to the parking location");
//        }
        if ((calculateDistance(parkingLocation, userLocation) > 100) && user.getRole() != Role.ADMIN) {
            return new BookingResponse(HttpStatus.FORBIDDEN.value(),"Please come closer to the parking location");
        }
    } catch (Exception e) {
        return new BookingResponse(HttpStatus.BAD_REQUEST.value(),"Error calculating distance");
    }

    int availableSlots = parkingService.fetchingAvailableSlots(request.getParkingId(), request.getStartTime(), request.getEndTime());
    if (availableSlots <= 0) {
        return new BookingResponse(HttpStatus.BAD_REQUEST.value(),"No available slots!");
    }



    Booking booking = new Booking();
    booking.setUser(user);
    booking.setParking(parking);
    booking.setStartTime(request.getStartTime());
    booking.setEndTime(request.getEndTime());
    booking.setStatus(Booking.Status.SUCCESS); // Assuming confirmed status for the new booking

    bookingDAO.save(booking);

    return new BookingResponse(booking);
}

    public List<BookingResponse> getBookingHistory(Long userId, User currentUser) {
        if (currentUser.getRole() != Role.ADMIN && !currentUser.getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to access this user's booking history!");
        }

        List<Booking> bookings;
        if (currentUser.getRole() == Role.ADMIN) {
            if (currentUser.getId().equals(userId)) {
                bookings = bookingDAO.findAll();
            } else {
                bookings = bookingDAO.findByUserId(userId);
            }
        } else {
            bookings = bookingDAO.findByUserId(userId);
        }

        return bookings.stream()
                .map(BookingResponse::new)
                .collect(Collectors.toList());
    }


    public String cancelBooking(Long userId, User requestingUser , Long bookingId) {

        Optional<Booking> cancelTheBooking = bookingDAO.findById(bookingId);

        Booking booking = null;

        if(cancelTheBooking.isPresent()){
            booking = cancelTheBooking.get();
        }

        if (requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(userId)) {
            return "You do not have permission to cancel this booking!";
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(booking.getStartTime()) ||
                (now.isAfter(booking.getStartTime()) && now.isBefore(booking.getEndTime()))) {

            if (booking.getStatus() == Booking.Status.CANCELLED) {
                return "Booking is already cancelled!";
            }

            booking.setStatus(Booking.Status.CANCELLED);
            bookingDAO.save(booking);

            Parking parking = booking.getParking();
            parking.incrementSlots();
            parkingDAO.save(parking);

            return "Booking cancelled successfully!";
        } else if (now.isAfter(booking.getEndTime())) {
            return "Your slot has ended, cancellation is not possible.";
        }

        return "Unknown error";
    }

    public String rescheduleBooking(RescheduleRequest request, HttpServletRequest token) {
        User requestingUser = userService.findUserByEmailViaCookie(token);

        Long bookingId = request.getBookingId();
        Long userId = request.getUserId();
        LocalDateTime newStartTime = null;
        LocalDateTime newEndTime = null;
        if(request.getNewStartTime() != null){
            newStartTime = request.getNewStartTime();
        }
        if(request.getNewEndTime() != null){
            newEndTime = request.getNewEndTime();
        }
        // Retrieve the user by ID
        Optional<User> userOptional = userDAO.findById(userId);
        if (userOptional.isEmpty()) {
            return "User not found!";
        }
        User user = userOptional.get();

        Optional<Booking> bookingOptional = bookingDAO.findById(bookingId);
        if (bookingOptional.isEmpty()) {
            return "Booking not found!";
        }
        Booking booking = bookingOptional.get();

        if (requestingUser.getRole() != Role.ADMIN && !requestingUser.getId().equals(userId)) {
            return "You do not have permission to reschedule this booking!";
        }
        if (!booking.getUser().getId().equals(userId)) {
            return "Booking does not belong to the provided user ID!";
        }

        Parking parking = booking.getParking();

        if (newStartTime != null) {
            booking.setStartTime(newStartTime);
        }
        if (newEndTime != null) {
            booking.setEndTime(newEndTime);
        }
        bookingDAO.save(booking);

        return "Booking rescheduled successfully!";
    }

    public List<Booking> getBookedSlots(String token) {
        User user = userService.findUserByEmailViaToken(token);
        if (user == null) {
            throw new NotFoundException("User not found!");
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
        double lat1 = point1.getY();
        double lon1 = point1.getX();
        double lat2 = point2.getY();
        double lon2 = point2.getX();

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

    @Transactional
    public List<Booking> getAllBookingHistory(User user, int page, int size) {
        return bookingDAO.getAllBookingHistory(user, page, size);
    }

    public long getTotalBookingCount(User user) {
        return bookingDAO.getTotalBookingCount(user);
    }
}
