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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
@Log4j2
@Service
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
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

    @Value("${upload.directory}")
    private String UPLOAD_DIR;

    //private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

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

            if(request.getContactNumber() != null && !request.getContactNumber().isEmpty()) {
                user.setContactNumber(request.getContactNumber());
            }

            if(request.getProfilePic() != null && !request.getProfilePic().isEmpty()) {
                user.setProfilePic(request.getProfilePic());
            }

            if(request.getDepartment() != null && !request.getDepartment().isEmpty()) {
                user.setDepartment(request.getDepartment());
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

    public User flagUser(Long userId, int days) {
        User user = userDAO.findById(userId).orElse(null);

        if (user == null) {
            log.info("User not found");
            throw new NotFoundException("User not found.");
        }

        // Set the allowed_after timestamp to the current time + (days * 86400 seconds)

        LocalDateTime allowedAfter = LocalDateTime.now().plusDays(days);
        long allowedAfterMillis = allowedAfter.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        user.setAllowedAfter(allowedAfterMillis);

        // Save the user with the updated allowed_after field
        userDAO.save(user);
        log.info("Flag user " + userId + " to " + days + " days");
        return user;
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


    public User findUserByEmailViaToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedException("Authorization token is missing or invalid");
        }

        // Extract email from the token
        String email = jwtUtil.extractEmail(token);
        if (email == null) {
            throw new UnauthorizedException("Email is missing in the token");
        }

        // Fetch the user by email
        User user = findByEmail(email);
        return user;
    }

    public User findUserByEmailViaCookie(HttpServletRequest request) {
        // Get all cookies from the request
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            throw new UnauthorizedException("JWT cookie is missing");
        }

        String token = null;

        // Find the cookie with the name "jwt"
        for (Cookie cookie : cookies) {
            if ("jwt".equals(cookie.getName())) {
                token = cookie.getValue();
                break;
            }
        }

        if (token == null) {
            throw new UnauthorizedException("JWT token is missing or invalid");
        }

        // Extract email from the token
        String email = jwtUtil.extractEmail(token);
        if (email == null) {
            throw new UnauthorizedException("Email is missing in the token");
        }

        // Fetch the user by email
        User user = findByEmail(email);
        return user;
    }

    public User uploadProfileImage(MultipartFile file, Long userId) throws IOException {
        // Retrieve the user

        User user = userDAO.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate a unique filename for the uploaded file
        String filename = userId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Create the full path for where the file will be stored
        Path path = Paths.get(UPLOAD_DIR + filename);

        // Ensure the directory exists; if not, create it
        Files.createDirectories(path.getParent());  // This will create the directory if it doesn't exist

        // Transfer the file to the specified path
        try {
            file.transferTo(path.toFile()); // Save the file to disk
        } catch (IOException e) {
            throw new IOException("Failed to upload file: " + e.getMessage(), e);
        }

        // Update the user's profilePic field with the new filename
        user.setProfilePic(filename);

        // Save the updated user entity back to the database
        userDAO.save(user);

        return user;  // Return the updated user with profile image
    }

    public User removeProfileImage(Long userId) throws Exception {
        User user = userDAO.findById(userId).orElse(null);

        // Set the profile image field to null
        if(user == null){
            throw new NotFoundException("User not found");
        }
        user.setProfilePic(null);
        // Save the updated user object to the database
        return userDAO.save(user);
    }

    public Optional<List<User>> getAllUsers() {
        List<User> users = userDAO.findAll();
        return Optional.of(users);
    }
}
