package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Booking;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
@Transactional
public class BookingDAOJpaImpl implements BookingDAO {

    private EntityManager entityManager;

    @Autowired
    public BookingDAOJpaImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Booking save(Booking booking) {
        if (booking.getId() == null) {
            entityManager.persist(booking);
            return booking;
        } else {
            return entityManager.merge(booking);
        }
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Booking.class, id));
    }

    @Override
    public List<Booking> findAll() {
        return entityManager.createQuery("SELECT b FROM Booking b", Booking.class).getResultList();
    }

    @Override
    public void deleteById(Long id) {
        Booking booking = entityManager.find(Booking.class, id);
        if (booking != null) {
            entityManager.remove(booking);
        }
    }
}
