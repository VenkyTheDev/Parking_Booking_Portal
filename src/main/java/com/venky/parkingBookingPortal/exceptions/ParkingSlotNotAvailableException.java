package com.venky.parkingBookingPortal.exceptions;

public class ParkingSlotNotAvailableException extends RuntimeException {
    public ParkingSlotNotAvailableException(String message) {
        super(message);
    }
}

