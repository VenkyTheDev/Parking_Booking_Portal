package com.venky.parkingBookingPortal.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.venky.parkingBookingPortal.config.PointSerializer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Point;



@Entity
@Table(name = "parking")
@Getter
@Setter
public class Parking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(nullable = false, length = 255, name = "name")
    private String name;

    @JsonSerialize(using = PointSerializer.class)
    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Column(nullable = false, name = "total_slots")
    private Integer totalSlots;

    @Column(nullable = false)
    private int highestSlots;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(columnDefinition = "TEXT")
    private String parkingImage;

    public void decrementSlots() {
        if (this.totalSlots > 0) {
            this.totalSlots -= 1;
        } else {
            throw new IllegalArgumentException("No available slots");
        }
    }

    public void incrementSlots() {
            this.totalSlots++;
    }

}
