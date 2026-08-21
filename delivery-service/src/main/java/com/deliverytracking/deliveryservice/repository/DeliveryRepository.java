package com.deliverytracking.deliveryservice.repository;

import com.deliverytracking.deliveryservice.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
}
