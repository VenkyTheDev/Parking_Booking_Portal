package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.BookingDAO;
import com.venky.parkingBookingPortal.dao.ParkingDAO;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.Parking;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ParkingService {

    private final ParkingDAO parkingDAO;
    private final BookingDAO bookingDAO;

    @Autowired
    public ParkingService(ParkingDAO parkingDAO , BookingDAO bookingDAO) {
        this.parkingDAO = parkingDAO;
        this.bookingDAO = bookingDAO;
    }

    public int getAvailableSlots(Long parkingId) {
        Parking parking = parkingDAO.findById(parkingId)
                .orElseThrow(() -> new NotFoundException("Parking lot not found"));

        return parking.getTotalSlots(); // Assuming you have a method for available slots
    }

    @Scheduled(fixedRate = 60000)  // Runs every minute (adjust as needed)
    public void updateAvailableSlotsForToday() {
        // Get the current date without time (i.e., start of the day)
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();  // Current time

        // Get all bookings with SUCCESS status that have ended today
        List<Booking> endedBookingsToday = bookingDAO.findByEndTimeBetweenAndStatus(startOfDay, now, Booking.Status.SUCCESS);

        // Process each booking and increment the slots if it has ended and is successful
        for (Booking booking : endedBookingsToday) {
            Parking parking = booking.getParking();

            // If the end time has passed, increment the slots
            if (booking.getEndTime().isBefore(now)) {
                parking.incrementSlots();  // Increment the available slots
                parkingDAO.save(parking);  // Save the updated parking
            }
        }
    }

}
