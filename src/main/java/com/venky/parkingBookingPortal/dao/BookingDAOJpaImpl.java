package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Booking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
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

    @Override
    public void deleteByUserId(Long userId) {
        entityManager.createQuery("UPDATE Booking b SET b.isDeleted = true WHERE b.user.id = :userId")
                .setParameter("userId", userId)
                .executeUpdate();
    }


    @Override
    public List<Booking> findByUserId(Long userId) {
        TypedQuery<Booking> query = entityManager.createQuery(
                "SELECT b FROM Booking b WHERE b.user.id = :userId", Booking.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }

    @Override
    public Optional<Booking> findFirstByUserIdAndStatusOrderByStartTimeDesc(Long userId, Booking.Status status) {
        try {
            Booking booking = entityManager.createQuery(
                            "SELECT b FROM Booking b WHERE b.user.id = :userId AND b.status = :status " +
                                    "ORDER BY b.startTime DESC", Booking.class)
                    .setParameter("userId", userId)
                    .setParameter("status", status)
                    .setMaxResults(1) // Fetch only the latest booking
                    .getSingleResult();

            return Optional.of(booking);
        } catch (NoResultException e) {
            return Optional.empty(); // Return empty if no matching booking is found
        }
    }


    @Override
    public Optional<Booking> findFirstByUserIdOrderByStartTimeDesc(Long userId) {
        try {
            // Fetch the latest booking regardless of its status
            Booking booking = entityManager.createQuery(
                            "SELECT b FROM Booking b WHERE b.user.id = :userId " +
                                    "ORDER BY b.startTime DESC, b.createdAt DESC", Booking.class)
                    .setParameter("userId", userId)
                    .setMaxResults(1) // Fetch only the latest booking
                    .getSingleResult();

            return Optional.of(booking);
        } catch (NoResultException e) {
            return Optional.empty(); // Return empty if no matching booking is found
        }
    }


    @Override
    public long countByParkingAndTimeRange(Long parkingId, LocalDateTime startTime, LocalDateTime endTime) {
        String jpql = "SELECT COUNT(b) FROM Booking b WHERE b.parking.id = :parkingId AND " +
                "((b.startTime BETWEEN :startTime AND :endTime) OR (b.endTime BETWEEN :startTime AND :endTime))";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("parkingId", parkingId);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);

        return query.getSingleResult();
    }

    @Override
    public boolean existsByParkingIdAndEndTimeBefore(Long parkingId, LocalDateTime endTime) {
        String jpql = "SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
                "FROM Booking b WHERE b.parking.id = :parkingId AND b.endTime < :endTime";

        // Create the query using EntityManager
        Query query = entityManager.createQuery(jpql);
        query.setParameter("parkingId", parkingId);
        query.setParameter("endTime", endTime);

        // Execute the query and get the result (true/false)
        Boolean result = (Boolean) query.getSingleResult();
        return result != null && result;  // Return the result as boolean
    }

    @Override
    public List<Booking> findByEndTimeBetweenAndStatusAndProcessedFalse(LocalDateTime startTime, LocalDateTime endTime, Booking.Status status) {
        String query = "SELECT b FROM Booking b WHERE b.endTime BETWEEN :startTime AND :endTime " +
                "AND b.status = :status AND b.processed = false";

        // Create a TypedQuery
        TypedQuery<Booking> typedQuery = entityManager.createQuery(query, Booking.class);

        // Set parameters for the query
        typedQuery.setParameter("startTime", startTime);
        typedQuery.setParameter("endTime", endTime);
        typedQuery.setParameter("status", status);

        // Execute the query and return the result list
        return typedQuery.getResultList();
    }
}
