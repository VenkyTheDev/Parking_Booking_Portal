package com.venky.parkingBookingPortal.dto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RescheduleRequest {

    private Long bookingId;
    private Long userId;
    private LocalDateTime newStartTime;
    private LocalDateTime newEndTime;
}
