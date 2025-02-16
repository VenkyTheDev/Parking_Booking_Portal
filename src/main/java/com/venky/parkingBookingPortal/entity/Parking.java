package com.venky.parkingBookingPortal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "parking")  // Table name in the database
@Getter
@Setter
public class Parking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)  // Primary Key
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = false)  // Foreign Key to Organisation
    private Organisation organisation;

    @Column(nullable = false, length = 255, name = "name")  // Name of the parking lot
    private String name;

    @Column(name = "location", columnDefinition = "GEOMETRY")  // Geographical location
    private String location;

    @Column(nullable = false, name = "total_slots")  // Total number of parking slots
    private Integer totalSlots;

    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL)
    private List<Booking> bookings;  // One Parking → Many Bookings
}
