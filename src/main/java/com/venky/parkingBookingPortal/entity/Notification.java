package com.venky.parkingBookingPortal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Getter
@Setter
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false, name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)  // Foreign Key to User
    private User user;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)  // Foreign Key to Booking
    private Booking booking;

    @Column(nullable = false, name = "message")
    private String message;  // Message content for the notification

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;  // Timestamp of notification creation
}
