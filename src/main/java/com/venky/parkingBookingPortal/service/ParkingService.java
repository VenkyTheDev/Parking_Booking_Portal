package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.ParkingDAO;
import com.venky.parkingBookingPortal.entity.Parking;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParkingService {

    private final ParkingDAO parkingDAO;

    @Autowired
    public ParkingService(ParkingDAO parkingDAO) {
        this.parkingDAO = parkingDAO;
    }

    public int getAvailableSlots(Long parkingId) {
        Parking parking = parkingDAO.findById(parkingId)
                .orElseThrow(() -> new NotFoundException("Parking lot not found"));

        return parking.getTotalSlots(); // Assuming you have a method for available slots
    }
}
