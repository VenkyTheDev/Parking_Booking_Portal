//package com.venky.parkingBookingPortal.entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "booking")
//@Getter
//@Setter
//public class Booking {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(unique = true, nullable = false, name = "id")
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(nullable = false)  // Foreign Key to User
//    private User user;
//
//    @ManyToOne
//    @JoinColumn(nullable = false)  // Foreign Key to Parking
//    private Parking parking;
//
//    @Column(nullable = false)
//    private LocalDateTime startTime;  // Start time of the booking
//
//    @Column(nullable = false)
//    private LocalDateTime endTime;  // End time of the booking
//
//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private Status status;  // Status of the booking (Success / Failed)
//
//    // Enum for booking status
//    public enum Status {
//        SUCCESS, CANCELLED, FAILED
//    }
//}

package com.venky.parkingBookingPortal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(nullable = false)  // Foreign Key to User
    private User user;

    @ManyToOne
    @JoinColumn(nullable = false)  // Foreign Key to Parking
    private Parking parking;

    @Column(nullable = false)
    private LocalDateTime startTime;  // Start time of the booking

    @Column(nullable = false)
    private LocalDateTime endTime;  // End time of the booking

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;  // Status of the booking (Success / Cancelled / Failed)

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // Auto-generated timestamp when the booking is created

    // Enum for booking status
    public enum Status {
        SUCCESS, CANCELLED, FAILED
    }
}

