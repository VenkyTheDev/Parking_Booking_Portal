package com.venky.parkingBookingPortal.controller;

import com.venky.parkingBookingPortal.dto.GetAvailableSlotsRequest;
import com.venky.parkingBookingPortal.entity.Parking;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import com.venky.parkingBookingPortal.service.ParkingService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Log4j2
@RestController
@RequestMapping("/api/parking")
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

//    @GetMapping("/{parkingId}/available-slots")
//    public ResponseEntity<?> getAvailableSlots(@PathVariable Long parkingId) {
//        try {
//            int availableSlots = parkingService.getAvailableSlots(parkingId);
//            return ResponseEntity.ok(availableSlots);
//        } catch (NotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
//        }
//    }

//    @GetMapping("/getall")
//    public ResponseEntity<?> getAllParkings() {
//        try {
//            return ResponseEntity.ok(parkingService.getAllParkings());
//        } catch (NotFoundException e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
//        }
//    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAllParkings(@RequestParam(required = false) LocalDateTime endTime) {
        try {
            return ResponseEntity.ok(parkingService.getAllParkings(endTime));
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }
    }


    @PostMapping("/nearestSlot")
    public ResponseEntity<?> getAvailableSlots(@RequestBody GetAvailableSlotsRequest request) {
        LocalDateTime availableSlots = parkingService.getNearestParkingTime(request.getParkingId() , request.getStartTime() , request.getEndTime());
        return ResponseEntity.ok(availableSlots);
    }

    @PostMapping("/add-parking-image")
    public ResponseEntity<Parking> addParkingImage(

            @RequestParam("file") MultipartFile file,
            @RequestParam("parkingId") Long parkingId) {
        try {
            // Upload the parking image using the service and get the updated Parking object
            Parking updatedParking = parkingService.uploadParkingImage(file, parkingId);

            // Return the updated Parking in the response with HTTP 200 OK
            return ResponseEntity.ok(updatedParking);
        } catch (IOException e) {
            e.printStackTrace();

            // Return error response with INTERNAL_SERVER_ERROR status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

}