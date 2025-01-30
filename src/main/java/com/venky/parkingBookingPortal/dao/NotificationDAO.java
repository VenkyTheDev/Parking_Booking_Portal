package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Notification;
import java.util.List;
import java.util.Optional;

public interface NotificationDAO {
    Notification save(Notification notification);

    Optional<Notification> findById(Long id);

    List<Notification> findAll();

    void deleteById(Long id);
}
