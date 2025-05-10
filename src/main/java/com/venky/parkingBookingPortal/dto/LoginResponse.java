package com.venky.parkingBookingPortal.dto;

import com.venky.parkingBookingPortal.entity.User;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class LoginResponse {
    private User user;  // Full User object
    private String token;
    private String message;

    public LoginResponse(User user, String token, String message) {
        this.user = user;
        this.token = token;
        this.message = message;
    }
}


