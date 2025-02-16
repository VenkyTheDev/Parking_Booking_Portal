package com.venky.parkingBookingPortal.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(min = 3, max = 50)
    private String name;

    @Email
    private String email;

    @Size(min = 6, max = 100, message = "Password should be at least 6 characters")
    private String password;

    private String contactNumber;

    private String profilePic;

    private String department;
}

