package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.BookingDAO;
import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dto.UpdateProfileRequest;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.Role;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.ForbiddenException;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import com.venky.parkingBookingPortal.exceptions.UnauthorizedAccessException;
import com.venky.parkingBookingPortal.exceptions.UnauthorizedException;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;

    private final BookingDAO bookingDAO;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserDAO userDAO , BookingDAO bookingDAO, JwtUtil jwtUtil) {
        this.userDAO = userDAO;
        this.bookingDAO = bookingDAO;
        this.jwtUtil = jwtUtil;
    }

    public Optional<User> updateUser(Long userId, UpdateProfileRequest request) {
        Optional<User> userOptional = userDAO.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Update name and email
            if (request.getName() != null && !request.getName().isEmpty()) {
                user.setName(request.getName());
            }

            // Preserve existing email if not provided
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                user.setEmail(request.getEmail());
            }

            // If a new password is provided
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
                user.setPassword(hashedPassword);
            }

            userDAO.save(user);
            return Optional.of(user);
        }

        return Optional.empty();
    }

    public boolean deleteUserByAdmin(Long userId, String token) throws UnauthorizedException, ForbiddenException {

        User requestingUser = findUserByEmailViaToken(token);

        // Check if the requesting user is an admin
        if (requestingUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You do not have permission to delete a user.");
        }

        // Proceed with the user deletion if the role is admin
        Optional<User> userOptional = userDAO.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Explicitly mark the user as deleted (soft delete)
            user.setDeleted(true);

            // Save the updated user to the database
            userDAO.save(user);

            // Optionally, delete all bookings for this user (depending on your requirements)
            bookingDAO.deleteByUserId(userId);

            return true;
        }

        return false;  // User not found
    }

    public String flagUser(Long userId, int days) {
        User user = userDAO.findById(userId).orElse(null);

        if (user == null) {
            return "User not found";
        }

        // Set the allowed_after timestamp to the current time + (days * 86400 seconds)

        LocalDateTime allowedAfter = LocalDateTime.now().plusDays(2);
        long allowedAfterMillis = allowedAfter.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        user.setAllowedAfter(allowedAfterMillis);

        // Save the user with the updated allowed_after field
        userDAO.save(user);

        return "User flagged successfully, booking allowed after: " + allowedAfterMillis;
    }

    public String unflagUser(Long userId) {
        Optional<User> userOptional = userDAO.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Reset allowed_after to current epoch time
            long allowedAfterMillis = System.currentTimeMillis();
            user.setAllowedAfter(allowedAfterMillis); // current epoch time
            userDAO.save(user);
            return "User unflagged successfully!";
        }

        return "User not found!";
    }

//    public User getProfile(Long userId, String email) {
    public User getProfile(Long userId, User requestingUser ) {
//        // Retrieve the requesting user using the email
//        Optional<User> requestingUserOptional = userDAO.findByEmail(email);
//        if (requestingUserOptional.isEmpty()) {
//            throw new UnauthorizedAccessException("Invalid token or user not found.");
//        }

        // If the requesting user is an admin, they can access any user's profile
        if (requestingUser.getRole() == Role.ADMIN || requestingUser.getId().equals(userId)) {
            Optional<User> userOptional = userDAO.findById(userId);
            if (userOptional.isEmpty()) {
                throw new NotFoundException("User not found.");
            }
            return userOptional.get();
        } else {
            throw new UnauthorizedAccessException("You do not have permission to view this profile.");
        }
    }

    public User findById(Long id) {
        return userDAO.findById(id).orElse(null);
    }

    public User findByEmail(String email) {
        return userDAO.findByEmail(email).orElse(null);
    }

    public List<Booking> findAllActiveBookingsList(Long userId){
        return bookingDAO.findActiveBookings(userId);
    }

    public User findUserByEmailViaToken(String authHeader){
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Authorization token is missing or invalid");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        // Extract email from the token
        String email = jwtUtil.extractEmail(token);
        if (email == null) {
            throw new UnauthorizedException("Email is missing in the token");
        }

        User user = findByEmail(email);
        return user;
    }

}
