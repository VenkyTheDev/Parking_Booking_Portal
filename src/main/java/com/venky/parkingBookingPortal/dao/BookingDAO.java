package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingDAO {
    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findAll();

    void deleteById(Long id);

    List<Booking> findByUserId(Long userId);
}
