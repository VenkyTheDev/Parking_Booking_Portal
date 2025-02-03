package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.BookingDAO;
import com.venky.parkingBookingPortal.dao.ParkingDAO;
import com.venky.parkingBookingPortal.entity.Booking;
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
        Parking parking = parkingDAO.findById(parkingId)
                .orElseThrow(() -> new NotFoundException("Parking lot not found"));

        return parking.getTotalSlots(); // Assuming you have a method for available slots
    }

    @Scheduled(fixedRate = 6000)  // Runs every minute (adjust as needed)
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
}
