package com.deliverytracking.notificationservice.repository;

import com.deliverytracking.notificationservice.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtAsc(Long userId);
}
