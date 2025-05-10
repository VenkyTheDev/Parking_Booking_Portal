package com.venky.parkingBookingPortal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ParkingBookingPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParkingBookingPortalApplication.class, args);
	}
}