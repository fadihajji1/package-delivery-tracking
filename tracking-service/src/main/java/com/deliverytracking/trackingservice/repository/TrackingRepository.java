package com.deliverytracking.trackingservice.repository;

import com.deliverytracking.trackingservice.model.TrackingRecord;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TrackingRepository extends MongoRepository<TrackingRecord, String> {
    Optional<TrackingRecord> findByShipmentId(Long shipmentId);
}