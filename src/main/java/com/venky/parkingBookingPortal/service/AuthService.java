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


    public User registerUser(SignupRequest signupRequest) {
        // Check if the email already exists
        Optional<User> existingUser = userDAO.findByEmail(signupRequest.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Email is already taken");
        }

        User user = new User();
        user.setName(signupRequest.getName());
        user.setEmail(signupRequest.getEmail());
        user.setPassword(BCrypt.hashpw(signupRequest.getPassword(), BCrypt.gensalt()));
        Organisation organisation = organisationDAO.findOrganisationById(signupRequest.getOrganisationId());
        user.setOrganisation(organisation);
        user.setRole(Role.USER);

        userDAO.save(user);

        String jwtToken = jwtUtil.generateToken(user.getEmail());

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
