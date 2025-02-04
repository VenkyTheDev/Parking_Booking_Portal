package com.venky.parkingBookingPortal.dto;

import com.venky.parkingBookingPortal.entity.Booking;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

public class BookingResponse {
    private Long bookingId;
    private Long parkingId;
    private Point parkingLocation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    // Constructor
    public BookingResponse(Booking booking) {
        this.bookingId = booking.getId();
        this.parkingId = booking.getParking().getId();
        this.parkingLocation = booking.getParking().getLocation();
        this.startTime = booking.getStartTime();
        this.endTime = booking.getEndTime();
        this.status = booking.getStatus().name();
    }

    // Getters
    public Long getBookingId() {
        return bookingId;
    }

    public Long getParkingId() {
        return parkingId;
    }

    public Point getParkingLocation() {
        return parkingLocation;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }
}
