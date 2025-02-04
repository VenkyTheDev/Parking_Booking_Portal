package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Organisation;
import java.util.List;
import java.util.Optional;

public interface OrganisationDAO {
    Organisation save(Organisation organisation);

    Optional<Organisation> findById(Long id);

    List<Organisation> findAll();

    void deleteById(Long id);

    Organisation findOrganisationById(Long id);
}
