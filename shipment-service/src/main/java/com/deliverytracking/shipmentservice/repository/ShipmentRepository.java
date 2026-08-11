package com.deliverytracking.shipmentservice.repository;

import com.deliverytracking.shipmentservice.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
}