package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.entity.Organisation;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    void deleteById(Long id);

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndOrganisation(String email, Organisation organisation);
}
