package com.venky.parkingBookingPortal.dto;
import java.time.LocalDateTime;

public class RescheduleRequest {

    private Long bookingId;
    private Long userId;
    private LocalDateTime newStartTime;
    private LocalDateTime newEndTime;

    // Getters and Setters
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(LocalDateTime newStartTime) {
        this.newStartTime = newStartTime;
    }

    public LocalDateTime getNewEndTime() {
        return newEndTime;
    }

    public void setNewEndTime(LocalDateTime newEndTime) {
        this.newEndTime = newEndTime;
    }
}
