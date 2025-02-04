package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingDAO {
    Booking save(Booking booking);

    Optional<Booking> findById(Long id);

    List<Booking> findAll();

    void deleteById(Long id);

    List<Booking> findByUserId(Long userId);

    Optional<Booking> findFirstByUserIdAndStatusOrderByStartTimeDesc(Long userId, Booking.Status status);

    Optional<Booking> findFirstByUserIdOrderByStartTimeDesc(Long userId);

    List<Booking> findActiveBookings(Long userId);

    long countByParkingAndTimeRange(Long parkingId, LocalDateTime startTime, LocalDateTime endTime);


    void deleteByUserId(Long userId);

    boolean existsByParkingIdAndEndTimeBefore(Long parkingId, LocalDateTime endTime);

    List<Booking> findByEndTimeBetweenAndStatusAndProcessedFalse(LocalDateTime startTime, LocalDateTime endTime, Booking.Status status);

}
