package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dto.UpdateProfileRequest;
import com.venky.parkingBookingPortal.entity.User;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class UserService {

    private final UserDAO userDAO;

    @Autowired
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
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
    public boolean deleteUser(Long userId) {
        Optional<User> userOptional = userDAO.findById(userId);
        if (userOptional.isPresent()) {
            userDAO.deleteById(userId);
            return true;
        }
        return false;
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

}
