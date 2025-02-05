package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dto.ErrorResponse;
import com.venky.parkingBookingPortal.dto.ParkingSpaceEditRequest;
import com.venky.parkingBookingPortal.dto.ParkingSpaceRequest;
import com.venky.parkingBookingPortal.entity.Organisation;
import com.venky.parkingBookingPortal.entity.Parking;
import com.venky.parkingBookingPortal.entity.Role;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.ForbiddenException;
import com.venky.parkingBookingPortal.exceptions.UnauthorizedException;
import com.venky.parkingBookingPortal.service.OrganisationService;
import com.venky.parkingBookingPortal.service.ParkingService;
import com.venky.parkingBookingPortal.service.UserService;
import com.venky.parkingBookingPortal.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final OrganisationService organisationService;
    private final ParkingService parkingService;

    @Autowired
    public AdminController(UserService userService, JwtUtil jwtUtil, OrganisationService organisationService, ParkingService parkingService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.organisationService = organisationService;
        this.parkingService = parkingService;
    }

    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId,
                                        @RequestHeader("Authorization") String token) {
        try {
            // Call the service layer to handle the user deletion logic
            boolean isDeleted = userService.deleteUserByAdmin(userId, token);

            if (isDeleted) {
                return ResponseEntity.ok("User deleted successfully.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
            }
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (ForbiddenException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/flag/{userId}")
    public ResponseEntity<String> flagUser(@PathVariable Long userId, @RequestBody int days) {
        try {
            String result = userService.flagUser(userId, days);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while flagging the user.");
        }
    }

    @PostMapping("/unflag/{userId}")
    public ResponseEntity<String> unflagUser(@PathVariable Long userId) {
        String result = userService.unflagUser(userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/addParking")
    public ResponseEntity<?> addParkingSpace(@RequestHeader("Authorization") String authHeader,
                                             @RequestBody ParkingSpaceRequest request) {
        User admin = userService.findUserByEmailViaToken(authHeader);
        if (admin == null || admin.getRole() != Role.ADMIN) {
            return ResponseEntity.status(403).body(new ErrorResponse("Access denied: Only admins can add parking spaces.", 403));
        }

        // Fetch organisation
        Organisation organisation = organisationService.findById(request.getOrganisationId());
        if (organisation == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Organisation not found", 404));
        }

        // Create and save parking space
        Parking parkingSpace = parkingService.addParkingSpace(request);

        return ResponseEntity.ok(parkingSpace);
    }

    @PutMapping("/editParking/{parkingId}")
    public ResponseEntity<?> editParkingSpace(@RequestHeader("Authorization") String authHeader,
                                              @PathVariable Long parkingId,
                                              @RequestBody ParkingSpaceEditRequest request) {
        // Extract user from token
        User admin = userService.findUserByEmailViaToken(authHeader);

        // Ensure user is an admin
        if (admin == null || !Role.ADMIN.equals(admin.getRole())) {
            return ResponseEntity.status(403).body("Access denied: Only admins can modify parking spaces.");
        }

        // Fetch the existing parking space
        Parking existingParking = parkingService.findParkingById(parkingId);
        if (existingParking == null) {
            return ResponseEntity.badRequest().body("Parking space not found");
        }

        if(request.getHighestSlots() > 0){
            existingParking.setHighestSlots(request.getHighestSlots());
        }
        if(request.getName() != null){
            existingParking.setName(request.getName());
        }

        Parking updatedParking = parkingService.updateParkingSpace(existingParking);

        return ResponseEntity.ok(updatedParking);
    }

}
