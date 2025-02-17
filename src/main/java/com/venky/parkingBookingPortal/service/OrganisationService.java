package com.venky.parkingBookingPortal.service;

import com.venky.parkingBookingPortal.dao.OrganisationDAO;
import com.venky.parkingBookingPortal.dao.UserDAO;
import com.venky.parkingBookingPortal.dto.OrganisaitonRequest;
import com.venky.parkingBookingPortal.entity.Organisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrganisationService {
    private final OrganisationDAO organizationDAO;
    private final UserDAO userDAO;

    @Autowired
    public OrganisationService(OrganisationDAO organizationDAO , UserDAO userDAO) {
        this.organizationDAO = organizationDAO;
        this.userDAO = userDAO;
    }

    public List<Organisation> getAllOrganisations() {
        return organizationDAO.findAll();
    }

    public Organisation findById(Long id) {
        return organizationDAO.findOrganisationById(id); // Ensure it returns Organisation
    }

    public Organisation saveOrganisation(OrganisaitonRequest organisationRequest) {
        if (organisationRequest == null) {
            return null;
        }
        if(organisationRequest.getName() == null || organisationRequest.getName().trim().equals("")){
            return null;
        }
        Organisation organisation = organizationDAO.add(organisationRequest);
        return organisation;
    }
}
