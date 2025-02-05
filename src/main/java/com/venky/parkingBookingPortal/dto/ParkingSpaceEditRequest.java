package com.venky.parkingBookingPortal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParkingSpaceEditRequest {
    private int highestSlots;
    private String name;
}
