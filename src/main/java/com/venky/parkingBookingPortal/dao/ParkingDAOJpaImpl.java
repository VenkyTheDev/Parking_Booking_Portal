package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Parking;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
@Transactional
public class ParkingDAOJpaImpl implements ParkingDAO{
    private EntityManager entityManager;

    @Autowired
    public ParkingDAOJpaImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Parking save(Parking parking) {
        if (parking.getId() == null) {
            entityManager.persist(parking);
            return parking;
        } else {
            return entityManager.merge(parking);
        }
    }

    @Override
    public Optional<Parking> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Parking.class, id));
    }

    @Override
    public List<Parking> findAll() {
        return entityManager.createQuery("SELECT p FROM Parking p", Parking.class).getResultList();
    }

    @Override
    public Parking findParkingId(Long id) {
        return entityManager.find(Parking.class, id);
    }

    @Override
    public void deleteById(Long id) {
        Parking parking = entityManager.find(Parking.class, id);
        if (parking != null) {
            entityManager.remove(parking);
        }
    }
}
