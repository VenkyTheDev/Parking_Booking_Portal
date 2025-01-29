package com.venky.parkingBookingPortal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")  // Explicitly naming the table
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "organisation_id", nullable = false)  // Foreign Key to Organisation
    private Organisation organisation;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;  // Enum for User/Admin

    @Column(length = 255)
    private String department;

    @Column(length = 20)
    private String contactNumber;

    @Column(columnDefinition = "TEXT")  // Storing profile picture as a URL or Base64 string
    private String profilePic;

    @Column(columnDefinition = "GEOMETRY")  // For storing location
    private String currentLocation;

    @Column(nullable = false)
    private Long allowedAfter;  // Epoch timestamp for permission

}
