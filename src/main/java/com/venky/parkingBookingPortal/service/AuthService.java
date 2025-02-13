package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dto.LoginRequest;
import com.venky.parkingBookingPortal.dto.LoginResponse;
import com.venky.parkingBookingPortal.dto.SignupRequest;
import com.venky.parkingBookingPortal.entity.Organisation;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dao.OrganisationDAO;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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



//    public String registerUser(SignupRequest request) {
//        // Check if the organisation exists
//        Optional<Organisation> organisationOpt = organisationDAO.findById(request.getOrganisationId());
//        if (organisationOpt.isEmpty()) {
//            throw new RuntimeException("Organisation not found with ID: " + request.getOrganisationId());
//        }
//
//        Organisation organisation = organisationOpt.get();
//
//        // Check if user exists under the given organisation
//        Optional<User> existingUser = userDAO.findByEmailAndOrganisation(request.getEmail(), organisation);
//        if (existingUser.isPresent()) {
//            throw new RuntimeException("User with email " + request.getEmail() + " already exists in this organisation.");
//        }
//
//        // Create new user
//        User user = new User();
//        user.setName(request.getName());
//        user.setEmail(request.getEmail());
//
//        // Hash password using BCrypt
//        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
//        user.setPassword(hashedPassword);
//        user.setRole(Role.USER);  // Assign default role
//        user.setOrganisation(organisation);  // Associate user with organisation
//
//        // Save user
//        userDAO.save(user);
//
//        // Generate and return JWT token
//        return jwtUtil.generateToken(user.getEmail());
//    }

//    public String authenticateUser(LoginRequest loginRequest) {
//        // Retrieve the user by email
//        Optional<User> userOptional = userDAO.findByEmail(loginRequest.getEmail());
//
//        // If the user doesn't exist, return null or throw an exception (optional)
//        if (userOptional.isEmpty()) {
//            throw new RuntimeException("User not found");
//        }
//
//        User user = userOptional.get();
//
//        // Verify the password using BCrypt
//        if (BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
//            // Generate JWT token if credentials are valid
//            return jwtUtil.generateToken(user.getEmail());
//        } else {
//            throw new RuntimeException("Invalid credentials");
//        }
//    }

    public User registerUser(SignupRequest signupRequest) {
        // Check if the email already exists
        Optional<User> existingUser = userDAO.findByEmail(signupRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email is already taken");
        }

        // Create a new user object
        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(BCrypt.hashpw(signupRequest.getPassword(), BCrypt.gensalt()));
        Organisation organisation = organisationDAO.findOrganisationById(signupRequest.getOrganisationId());
        user.setOrganisation(organisation);
        user.setRole(Role.USER);

        // Save the user to the database
        userDAO.save(user);

        // Generate JWT token for the newly registered user
        String jwtToken = jwtUtil.generateToken(user.getEmail());

        // Return LoginResponse containing full user object and token
        return user;
    }


    public LoginResponse authenticateUser(LoginRequest loginRequest, HttpServletResponse httpServletResponse) {
        // Retrieve user from database
        Optional<User> userOptional = userDAO.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOptional.get();

        // Verify password using BCrypt
        if (BCrypt.checkpw(loginRequest.getPassword(), user.getPassword())) {
            // Generate JWT token
            String jwtToken = jwtUtil.generateToken(user.getEmail());

            // Create an HTTP-only cookie with the JWT token
            Cookie jwtCookie = new Cookie("jwt", jwtToken);
            jwtCookie.setHttpOnly(true); // Protect against XSS
            jwtCookie.setSecure(true);   // Ensure it's sent over HTTPS
            jwtCookie.setPath("/");      // Available across the entire app
            jwtCookie.setMaxAge(24 * 60 * 60); // 1 day expiration

            // Add the cookie to the HTTP response
            httpServletResponse.addCookie(jwtCookie);

            // Return LoginResponse containing full user object and token
            return new LoginResponse(user, jwtToken, "Login successful!");
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }


}
