package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.OrganisationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrganisationService {
    private final OrganisationDAO organizationDAO;

    @Autowired
    public OrganisationService(OrganisationDAO organizationDAO) {
        this.organizationDAO = organizationDAO;
    }

    public List<Map<String, Object>> getAllOrganisations() {
        return organizationDAO.findAll()
                .stream()
                .map(organisation -> {
                    Map<String, Object> organisationData = new HashMap<>();
                    organisationData.put("id", organisation.getId());
                    organisationData.put("name", organisation.getName());
                    return organisationData;
                })
                .collect(Collectors.toList());
    }
}
