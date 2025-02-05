package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.entity.Organisation;
import com.venky.parkingBookingPortal.service.OrganisationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/organisations")
public class OrganisationController {

    private final OrganisationService organisationService;

    @Autowired
    public OrganisationController(OrganisationService organisationService) {
        this.organisationService = organisationService;
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
}
