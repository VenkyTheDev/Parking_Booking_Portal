package com.venky.parkingBookingPortal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "organisations")
@Getter
@Setter
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, length = 255, name = "name")
    private String name;

    @Column(length = 500, name = "address")
    private String address;

    @Column(length = 50, name = "contact_details")
    private String contactDetails;

    @Column(length = 255, name = "location")
    private String location;

    @Column(nullable = false, name = "total_parking_slots")
    private Integer totalParkingLots;

    @Column(nullable = false)
    private boolean isDeleted = false;

    // Remove bidirectional relationship. No need to maintain a list of users in the organisation entity.
}
