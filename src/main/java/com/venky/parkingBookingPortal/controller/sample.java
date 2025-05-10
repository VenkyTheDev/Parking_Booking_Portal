package com.venky.parkingBookingPortal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class sample {
    @GetMapping("/hello")
    public String hello(){
        return "Hello";
    }
}
