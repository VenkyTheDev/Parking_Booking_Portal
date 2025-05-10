package com.venky.parkingBookingPortal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParkingSpaceRequest {
    private Long organisationId;
    private int highestSlots;
    private String name;
    private String image;
    private Double latitude;
    private Double longitude;

}
