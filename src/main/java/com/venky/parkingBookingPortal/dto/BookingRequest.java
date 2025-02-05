package com.venky.parkingBookingPortal.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingRequest {
    private Long parkingId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Double latitude;
    private Double longitude;
    private Long userId;
}
