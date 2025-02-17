package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dto.OrganisaitonRequest;
import com.venky.parkingBookingPortal.entity.Organisation;
import com.venky.parkingBookingPortal.entity.Role;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.service.OrganisationService;
import com.venky.parkingBookingPortal.service.UserService;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/organisations")
public class OrganisationController {

    private final OrganisationService organisationService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @Autowired
    public OrganisationController(OrganisationService organisationService, JwtUtil jwtUtil, UserService userService) {
        this.organisationService = organisationService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllOrganizationNames() {
        try {
            // Get the list of organisations with both id and name
            List<Organisation> organizations = organisationService.getAllOrganisations();
            return ResponseEntity.ok(organizations);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching organization details.");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addOrganisation(@RequestBody OrganisaitonRequest organisationRequest, HttpServletRequest request) {
        try {
            // Fetch user from request
            User user = userService.findUserByEmailViaCookie(request);
            if (user == null || user.getRole() == Role.USER) {
                log.info("User Role: {}", (user != null) ? user.getRole().toString() : "NULL");
                log.warn("Unauthorized attempt to add an organisation");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not allowed to add an organisation.");
            }

            if (organisationRequest.getName() == null || organisationRequest.getName().trim().isEmpty()) {
                log.warn("Organisation name is missing in request");
                return ResponseEntity.badRequest().body("Organisation name is required.");
            }

            Organisation savedOrganisation = organisationService.saveOrganisation(organisationRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(savedOrganisation);

        } catch (Exception e) {
            log.error("Error while adding an organisation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while adding a new organisation.");
        }
    }

}
