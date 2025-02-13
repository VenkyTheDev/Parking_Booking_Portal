package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Booking;
import com.venky.parkingBookingPortal.entity.Parking;
import com.venky.parkingBookingPortal.entity.Role;
import com.venky.parkingBookingPortal.entity.User;
import com.venky.parkingBookingPortal.exceptions.NotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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
//    public List<Booking> findAll() {
//        return entityManager.createQuery("SELECT b FROM Booking b", Booking.class).getResultList();
//    }
    public List<Booking> findAll() {
        return entityManager.createQuery(
                        "SELECT b FROM Booking b JOIN FETCH b.user", Booking.class)
                .getResultList();
    }

    @Override
    public void deleteById(Long id) {
        Booking booking = entityManager.find(Booking.class, id);
        if (booking != null) {
            entityManager.remove(booking);
        }
    }

    @Override
    @Modifying
    @Transactional
    @org.springframework.data.jpa.repository.Query("DELETE FROM Booking b WHERE b.user.id = :userId")
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
    public List<Booking> findActiveBookings(Long userId) {
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            throw new NotFoundException("User not found!");
        }

        boolean isAdmin = user.getRole() == Role.ADMIN; // Checking if the user is an admin

        // Step 2: Fetch active bookings based on user role
        String queryStr = "SELECT b FROM Booking b " +
                "WHERE b.user.id = :userId " +
                "AND b.status = :status " +
                "AND b.endTime > CURRENT_TIMESTAMP " + // Ensuring booking is still active
                "ORDER BY b.startTime DESC, b.createdAt DESC";

        TypedQuery<Booking> query = entityManager.createQuery(queryStr, Booking.class)
                .setParameter("userId", userId)
                .setParameter("status", Booking.Status.SUCCESS);

        if (!isAdmin) {
            query.setMaxResults(1); // Normal users should only get their latest active booking
        }

        return query.getResultList(); // Returns a list (empty if no active bookings exist)
    }

    @Override
    public List<Booking> findAllActiveBookings() {
        String queryStr = "SELECT b FROM Booking b " +
                "WHERE b.status = :status " +
                "AND b.endTime > CURRENT_TIMESTAMP " + // Ensuring booking is still active
                "ORDER BY b.endTime, b.createdAt DESC";
        TypedQuery<Booking> query = entityManager.createQuery(queryStr, Booking.class)
                .setParameter("status", Booking.Status.SUCCESS);
        return query.getResultList();
    }


    @Override
    public long countByParkingAndTimeRange(Long parkingId, LocalDateTime startTime, LocalDateTime endTime) {
//        String jpql = "SELECT COUNT(b) FROM Booking b WHERE b.parking.id = :parkingId AND " +
//                "((b.startTime BETWEEN :startTime AND :endTime) OR (b.endTime BETWEEN :startTime AND :endTime))";
        String jpql = "SELECT COUNT(b) FROM Booking b WHERE b.parking.id = :parkingId AND " +
                "((b.startTime BETWEEN :startTime AND :endTime) OR (b.endTime BETWEEN :startTime AND :endTime) OR (b.startTime <= :startTime AND :endTime >= :endTime))";
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
    public List<Booking> findAllActiveBookingsBeforeEndTime(Long parkingId, LocalDateTime startTime, LocalDateTime endTime) {
//        String queryStr = "SELECT b FROM Booking b " +
//                "WHERE b.parking.id = :parkingId " +
//                "AND b.status = 'SUCCESS' " +
//                "AND b.startTime < :endTime " +   // Booking starts before the provided endTime
//                "AND b.endTime > :startTime " +
//                "AND b.processed = false "+// Booking ends after the provided startTime
//                "ORDER BY b.startTime DESC, b.createdAt DESC, b.endTime ASC";

        String queryStr = "SELECT b FROM Booking b " +
                "WHERE b.parking.id = :parkingId " +
                "AND b.status = 'SUCCESS' " +
                "AND ( " +
                "   (b.startTime <= :endTime AND b.startTime >= :startTime) " +   // Condition 1: start time overlaps with existing booking's time
                "   OR " +
                "   (b.endTime <= :endTime AND b.endTime >= :startTime) " +   // Condition 2: end time overlaps with existing booking's time
                "   OR " +
                "   (b.startTime <= :startTime AND b.endTime >= :endTime) " + // Condition 3: the new booking fully overlaps an existing booking
                ") " +
                "AND b.processed = false " +
                "ORDER BY b.startTime DESC, b.createdAt DESC, b.endTime ASC";


        TypedQuery<Booking> query = entityManager.createQuery(queryStr, Booking.class);
        query.setParameter("parkingId", parkingId);
        query.setParameter("startTime", startTime);
        query.setParameter("endTime", endTime);

        return query.getResultList();
    }

    @Override
    @Transactional
    public List<Booking> getAllBookingHistory(User user, int page, int size) {
        return entityManager.createQuery(
                        "SELECT b FROM Booking b WHERE b.user = :user ORDER BY b.createdAt DESC", Booking.class)
                .setParameter("user", user)
                .setFirstResult(page * size) // Offset calculation
                .setMaxResults(size) // Limit the number of results
                .getResultList();
    }

    @Override
    public long getTotalBookingCount(User user) {
        return entityManager.createQuery(
                        "SELECT COUNT(b) FROM Booking b WHERE b.user = :user", Long.class)
                .setParameter("user", user)
                .getSingleResult();
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
