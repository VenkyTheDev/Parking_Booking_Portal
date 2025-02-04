package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.BookingDAO;
import com.venky.parkingBookingPortal.dao.ParkingDAO;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.Organisation;
import com.venky.parkingBookingPortal.entity.Parking;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Log
@Slf4j
public class ParkingService {

    private final ParkingDAO parkingDAO;
    private final BookingDAO bookingDAO;

    @Autowired
    public ParkingService(ParkingDAO parkingDAO , BookingDAO bookingDAO) {
        this.parkingDAO = parkingDAO;
        this.bookingDAO = bookingDAO;
    }

    public int getAvailableSlots(Long parkingId) {
//        Parking parking = parkingDAO.findById(parkingId)
//                .orElseThrow(() -> new NotFoundException("Parking lot not found"));
        try {
            Parking parking = parkingDAO.findParkingId(parkingId);
            return parking.getTotalSlots();
        } catch (Exception e) {
            throw new NotFoundException("Parking lot not found");
        }
    }

    @Scheduled(fixedRate = 60000)  // Runs every minute (adjust as needed)
    public void updateAvailableSlotsForToday() {
        log.info("I'm in the updateAvailableSlotsForToday method");
        System.out.println("I'm in the updateAvailableSlotsForToday method");

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        // Get all unprocessed bookings with SUCCESS status that have ended today
        List<Booking> endedBookingsToday = bookingDAO.findByEndTimeBetweenAndStatusAndProcessedFalse(
                startOfDay, now, Booking.Status.SUCCESS
        );

        for (Booking booking : endedBookingsToday) {
            Parking parking = booking.getParking();

            if (booking.getEndTime().isBefore(now)) { // Ensure end time has passed
                parking.incrementSlots(); // Increment available slots
                parkingDAO.save(parking); // Save updated parking

                booking.setProcessed(true); // Mark booking as processed
                bookingDAO.save(booking); // Save updated booking
            }
        }

        log.info("I'm at the end of updateAvailableSlotsForToday method");
    }

    public Parking addParkingSpace(Organisation organisation, int highestSlots , String name) {
        // Create a new Parking object for the organisation with the highest slots
        Parking parking = new Parking();
        parking.setOrganisation(organisation); // Associate with the organisation
        parking.setTotalSlots(highestSlots);
        parking.setHighestSlots(highestSlots);// Set the number of parking slots
        parking.setName(name);

        // Save the parking space to the database
        return parkingDAO.save(parking); // Assuming save() returns the saved Parking entity
    }

    public Parking findParkingById(Long id) {
        return parkingDAO.findParkingId(id);
    }

    public Parking updateParkingSpace(Parking updatedParking) {
        try {
            // Fetch the existing parking space using ParkingDAO
            Parking existingParking = parkingDAO.findParkingId(updatedParking.getId());

            if (existingParking == null) {
                throw new NotFoundException("Parking space not found");
            }

            // Update the fields of the existing parking space with new values
            existingParking.setName(updatedParking.getName()); // Example of updating a field
            existingParking.setTotalSlots(updatedParking.getTotalSlots()); // Example of updating a field

            // You can update other fields as required

            // Save the updated parking space using ParkingDAO
            parkingDAO.save(existingParking);

            return existingParking; // Return the updated parking space
        } catch (Exception e) {
            // Log the exception if needed
            throw new RuntimeException("Error occurred while updating parking space", e);
        }
    }
}
