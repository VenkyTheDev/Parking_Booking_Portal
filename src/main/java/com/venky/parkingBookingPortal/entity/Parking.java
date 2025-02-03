package com.venky.parkingBookingPortal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

//@Entity
//@Table(name = "parking")  // Table name in the database
//@Getter
//@Setter
//public class Parking {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", unique = true, nullable = false)  // Primary Key
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "organisation_id", nullable = false)  // Foreign Key to Organisation
//    private Organisation organisation;
//
//    @Column(nullable = false, length = 255, name = "name")  // Name of the parking lot
//    private String name;
//
//    @Column(name = "location", columnDefinition = "GEOMETRY")  // Geographical location
//    private String location;
//
//    @Column(nullable = false, name = "total_slots")  // Total number of parking slots
//    private Integer totalSlots;
//
//    @OneToMany(mappedBy = "parking", cascade = CascadeType.ALL)
//    private List<Booking> bookings;
//
//    public void decrementSlots() {
//        if (this.totalSlots > 0) {
//            this.totalSlots -= 1;
//        } else {
//            throw new IllegalArgumentException("No available slots");
//        }
//    }// One Parking → Many Bookings
//
//    public void incrementSlots() {
//        if (this.totalSlots > 0) {
//            this.totalSlots++;
//        }
//    }
//
//}

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

    @Column(name = "location", columnDefinition = "GEOMETRY")
    private String location;

    @Column(nullable = false, name = "total_slots")
    private Integer totalSlots;

    public void decrementSlots() {
        if (this.totalSlots > 0) {
            this.totalSlots -= 1;
        } else {
            throw new IllegalArgumentException("No available slots");
        }
    }// One Parking → Many Bookings

    public void incrementSlots() {
        if (this.totalSlots > 0) {
            this.totalSlots++;
        }
    }

    // Removed the @OneToMany relationship with Booking to avoid cyclic dependency.
}
