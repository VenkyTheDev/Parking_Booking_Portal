package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.BookingDAO;
import com.venky.parkingBookingPortal.dao.OrganisationDAO;
import com.venky.parkingBookingPortal.dao.ParkingDAO;
import com.venky.parkingBookingPortal.dto.GetAvailableSlotsRequest;
import com.venky.parkingBookingPortal.dto.ParkingSpaceRequest;
import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.Organisation;
import com.venky.parkingBookingPortal.entity.Parking;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
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
    private final OrganisationDAO organisationDAO;

    GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @Autowired
    public ParkingService(ParkingDAO parkingDAO , BookingDAO bookingDAO , OrganisationDAO organisationDAO) {
        this.parkingDAO = parkingDAO;
        this.bookingDAO = bookingDAO;
        this.organisationDAO = organisationDAO;
    }

//    public int getAvailableSlots(Long parkingId) {
////        Parking parking = parkingDAO.findById(parkingId)
////                .orElseThrow(() -> new NotFoundException("Parking lot not found"));
//        try {
//            Parking parking = parkingDAO.findParkingId(parkingId);
//            return parking.getTotalSlots();
//        } catch (Exception e) {
//            throw new NotFoundException("Parking lot not found");
//        }
//    }

    public int getAvailableSlots(Long parkingId) {
    //        Parking parking = parkingDAO.findById(parkingId)
    //                .orElseThrow(() -> new NotFoundException("Parking lot not found"));
        try {
//            Parking parking = parkingDAO.findParkingId(parkingId);
//            return parking.getTotalSlots();
            int availableSlots = 0;
            return availableSlots;
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

    public Parking addParkingSpace(ParkingSpaceRequest request) {
        try{// Create a new Parking object for the organisation with the highest slots

            Parking parking = new Parking();
            Organisation organisation = organisationDAO.findOrganisationById(request.getOrganisationId());
            parking.setOrganisation(organisation); // Associate with the organisation
            parking.setTotalSlots(request.getHighestSlots());
            parking.setHighestSlots(request.getHighestSlots());// Set the number of parking slots
            parking.setName(request.getName());
            Point point = geometryFactory.createPoint(new Coordinate(request.getLongitude(), request.getLatitude()));
            parking.setLocation(point);
            // Save the parking space to the database
            return parkingDAO.save(parking);
        }catch (Exception e){
            throw new NotFoundException("Parking space not found");
        }
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

    public int fetchingAvailableSlots(Long parkingId , LocalDateTime startTime , LocalDateTime endTime) {
        List<Booking> ActiveBookings = bookingDAO.findAllActiveBookingsBeforeEndTime(parkingId , startTime , endTime);
        int totalActiveBookings = ActiveBookings.size();
        Parking parking = parkingDAO.findParkingId(parkingId);
        int totalActiveSlots = parking.getHighestSlots();
        return Math.max(0 , totalActiveSlots - totalActiveBookings);
    }

    public List<Parking> getAllParkings() {
        return parkingDAO.findAll();
    }

}
