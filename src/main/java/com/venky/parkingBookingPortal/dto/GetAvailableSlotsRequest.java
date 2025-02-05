package com.venky.parkingBookingPortal.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GetAvailableSlotsRequest {
    private Long parkingId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
