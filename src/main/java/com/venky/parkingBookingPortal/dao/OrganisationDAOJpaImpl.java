package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.dto.OrganisaitonRequest;
import com.venky.parkingBookingPortal.entity.Organisation;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Slf4j
@Repository
@Transactional
public class OrganisationDAOJpaImpl implements OrganisationDAO {

    private EntityManager entityManager;

    @Autowired
    public OrganisationDAOJpaImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Organisation save(Organisation organisation) {
        if (organisation.getId() == null) {
            entityManager.persist(organisation);
            return organisation;
        } else {
            return entityManager.merge(organisation);
        }
    }

    @Override
    public Optional<Organisation> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Organisation.class, id));
    }

    @Override
    public List<Organisation> findAll() {
        return entityManager.createQuery("SELECT o FROM Organisation o", Organisation.class).getResultList();
    }

    @Override
    public void deleteById(Long id) {
        Organisation organisation = entityManager.find(Organisation.class, id);
        if (organisation != null) {
            entityManager.remove(organisation);
        }
    }

    @Override
    public Organisation findOrganisationById(Long id) {
        return entityManager.find(Organisation.class, id);  // Fetching Organisation by ID using EntityManager
    }

    @Override
    public Organisation add(OrganisaitonRequest organisationRequest) {
        Organisation organisation = new Organisation();
        organisation.setName(organisationRequest.getName());
        organisation.setAddress(organisationRequest.getAddress());
        organisation.setContactDetails(organisationRequest.getContactDetails());
        organisation.setLocation(organisationRequest.getLocation());
        if(organisation.getTotalParkingLots() == null){
            organisation.setTotalParkingLots(organisationRequest.getTotalParkingSlots());
            log.info("This is the total number of parking lots in this organisation");
        }
        organisation.setTotalParkingLots(organisation.getTotalParkingLots());
        entityManager.persist(organisation);
        return organisation;
    }
}
