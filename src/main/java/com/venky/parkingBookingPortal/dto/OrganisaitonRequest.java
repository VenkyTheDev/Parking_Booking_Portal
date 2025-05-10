package com.venky.parkingBookingPortal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrganisaitonRequest {
    public String name;
    public String address;
    public String contactDetails;
    public String location;
    public Integer totalParkingSlots;
}
