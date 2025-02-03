package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Booking;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    long countByParkingAndTimeRange(Long parkingId, LocalDateTime startTime, LocalDateTime endTime);


    @Modifying
    @Transactional
    @Query("DELETE FROM Booking b WHERE b.user.id = :userId")
    void deleteByUserId(Long userId);

    boolean existsByParkingIdAndEndTimeBefore(Long parkingId, LocalDateTime endTime);

    List<Booking> findByEndTimeBetweenAndStatus(LocalDateTime startTime, LocalDateTime endTime, Booking.Status status);

}
