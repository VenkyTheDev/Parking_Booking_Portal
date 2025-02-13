package com.venky.parkingBookingPortal.dto;

import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.awt.*;

@Getter
@Setter
public class ParkingSpaceEditRequest {
    private Long id;
    private int highestSlots;
    private String name;
    private Double latitude;
    private Double longitude;
}
