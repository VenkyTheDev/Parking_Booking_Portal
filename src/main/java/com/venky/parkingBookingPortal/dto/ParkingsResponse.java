package com.venky.parkingBookingPortal.dto;

import com.venky.parkingBookingPortal.entity.Organisation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ParkingsResponse {
    private Long id;
    private Organisation organisation;
    private String name;
    private Point location;
    private Integer totalSlots;
    private int highestSlots;
    private boolean isDeleted = false;
    private String parkingImage;
    private int availableSlots;
    private LocalDateTime nearestParkingTime;
}
