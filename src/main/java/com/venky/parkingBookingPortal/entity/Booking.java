package com.venky.parkingBookingPortal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Getter
@Setter
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // Foreign Key to User
    private User user;

    @ManyToOne
    @JoinColumn(name = "parking_id", nullable = false)  // Foreign Key to Parking
    private Parking parking;

    @Column(nullable = false, name = "start_time")
    private LocalDateTime startTime;  // Start time of the booking

    @Column(nullable = false, name = "end_time")
    private LocalDateTime endTime;  // End time of the booking

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status")
    private Status status;  // Status of the booking (Success / Failed)

    // Enum for booking status
    public enum Status {
        SUCCESS, FAILED
    }
}
