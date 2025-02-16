package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Parking;
import java.util.List;
import java.util.Optional;

public interface ParkingDAO {
    Parking save(Parking parking);

    Optional<Parking> findById(Long id);

    List<Parking> findAll();

    void deleteById(Long id);
}
