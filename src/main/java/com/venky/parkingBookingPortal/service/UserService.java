package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.BookingDAO;
import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dto.UpdateProfileRequest;
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

import java.time.Instant;
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
//    public boolean deleteUser(Long userId) {
//        Optional<User> userOptional = userDAO.findById(userId);
//        if (userOptional.isPresent()) {
//            User user = userOptional.get();
//
//            // Explicitly delete all bookings for this user
//            bookingDAO.deleteByUserId(userId);
//
//            // Now delete the user
//            userDAO.deleteById(userId);
//            return true;
//        }
//        return false;
//    }

    public boolean deleteUserByAdmin(Long userId, String token) throws UnauthorizedException, ForbiddenException {
        // Extract email from the JWT token
        String email = jwtUtil.extractEmail(token);

        // Retrieve the requesting user from the database
        Optional<User> requestingUserOptional = userDAO.findByEmail(email);
        if (requestingUserOptional.isEmpty()) {
            throw new UnauthorizedException("Invalid token or user not found.");
        }

        User requestingUser = requestingUserOptional.get();

        // Check if the requesting user is an admin
        if (requestingUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You do not have permission to delete a user.");
        }

        // Proceed with the user deletion if the role is admin
        Optional<User> userOptional = userDAO.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Explicitly delete all bookings for this user
            bookingDAO.deleteByUserId(userId);

            // Now delete the user
            userDAO.deleteById(userId);
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
        long allowedAfterTimestamp = Instant.now().getEpochSecond() + (long)(days * 86400);

        user.setAllowedAfter(allowedAfterTimestamp);

        // Save the user with the updated allowed_after field
        userDAO.save(user);

        return "User flagged successfully, booking allowed after: " + allowedAfterTimestamp;
    }

    public String unflagUser(Long userId) {
        Optional<User> userOptional = userDAO.findById(userId);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Reset allowed_after to current epoch time
            user.setAllowedAfter(System.currentTimeMillis()); // current epoch time
            userDAO.save(user);
            return "User unflagged successfully!";
        }

        return "User not found!";
    }

    public User getProfile(Long userId, String email) {
        // Retrieve the requesting user using the email
        Optional<User> requestingUserOptional = userDAO.findByEmail(email);
        if (requestingUserOptional.isEmpty()) {
            throw new UnauthorizedAccessException("Invalid token or user not found.");
        }
        User requestingUser = requestingUserOptional.get();

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

}
