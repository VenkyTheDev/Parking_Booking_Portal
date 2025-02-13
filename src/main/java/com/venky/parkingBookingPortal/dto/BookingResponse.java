package com.venky.parkingBookingPortal.dto;

import com.venky.parkingBookingPortal.entity.Booking;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
@Getter
@Setter
public class BookingResponse {
    private Long bookingId;
    private Long parkingId;
    private Point parkingLocation;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    // Error handling fields
    private String errorMessage;
    private int statusCode;

    // Constructor for successful booking response
    public BookingResponse(Booking booking) {
        this.bookingId = booking.getId();
        this.parkingId = booking.getParking().getId();
        this.parkingLocation = booking.getParking().getLocation();
        this.startTime = booking.getStartTime();
        this.endTime = booking.getEndTime();
        this.status = booking.getStatus().name();
    }

    // Constructor for error response
    public BookingResponse(int statusCode, String errorMessage) {
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }

}
