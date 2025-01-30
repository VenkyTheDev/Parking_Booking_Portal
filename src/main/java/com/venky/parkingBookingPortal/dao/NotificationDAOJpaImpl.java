package com.venky.parkingBookingPortal.dao;

import com.venky.parkingBookingPortal.entity.Notification;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
@Transactional
public class NotificationDAOJpaImpl implements NotificationDAO {
    private EntityManager entityManager;

    @Autowired
    public NotificationDAOJpaImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    @Override
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            entityManager.persist(notification);
            return notification;
        } else {
            return entityManager.merge(notification);
        }
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Notification.class, id));
    }

    @Override
    public List<Notification> findAll() {
        return entityManager.createQuery("SELECT n FROM Notification n", Notification.class).getResultList();
    }

    @Override
    public void deleteById(Long id) {
        Notification notification = entityManager.find(Notification.class, id);
        if (notification != null) {
            entityManager.remove(notification);
        }
    }
}
