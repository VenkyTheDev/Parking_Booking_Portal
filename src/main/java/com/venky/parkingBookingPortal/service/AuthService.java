package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dto.LoginRequest;
import com.venky.parkingBookingPortal.dto.SignupRequest;
import com.venky.parkingBookingPortal.entity.Organisation;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dao.OrganisationDAO;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

import com.venky.parkingBookingPortal.entity.Role;

@Service
public class AuthService {

    private final UserDAO userDAO;
    private final OrganisationDAO organisationDAO;  // DAO for Organisation
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserDAO userDAO, OrganisationDAO organisationDAO, JwtUtil jwtUtil) {
        this.userDAO = userDAO;
        this.organisationDAO = organisationDAO;
        this.jwtUtil = jwtUtil;
    }

    public String registerUser(SignupRequest request) {
        // Check if the organisation exists
        Optional<Organisation> organisationOpt = organisationDAO.findById(request.getOrganisationId());
        if (organisationOpt.isEmpty()) {
            throw new RuntimeException("Organisation not found with ID: " + request.getOrganisationId());
        }

        Organisation organisation = organisationOpt.get();

        // Check if user exists under the given organisation
        Optional<User> existingUser = userDAO.findByEmailAndOrganisation(request.getEmail(), organisation);
        if (existingUser.isPresent()) {
            throw new RuntimeException("User with email " + request.getEmail() + " already exists in this organisation.");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        // Hash password using BCrypt
        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);
        user.setRole(Role.USER);  // Assign default role
        user.setOrganisation(organisation);  // Associate user with organisation

        // Save user
        userDAO.save(user);

        // Generate and return JWT token
        return jwtUtil.generateToken(user.getEmail());
    }

    public String authenticateUser(LoginRequest loginRequest) {
        // Retrieve the user by email
        Optional<User> userOptional = userDAO.findByEmail(loginRequest.getEmail());

        // If the user doesn't exist, return null or throw an exception (optional)
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();

        // Verify the password using BCrypt
        if (BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
            // Generate JWT token if credentials are valid
            return jwtUtil.generateToken(user.getEmail());
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

}
