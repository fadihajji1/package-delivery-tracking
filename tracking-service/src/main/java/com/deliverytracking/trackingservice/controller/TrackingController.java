package com.deliverytracking.trackingservice.controller;

import com.deliverytracking.trackingservice.model.TrackingRecord;
import com.deliverytracking.trackingservice.repository.TrackingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/tracking")
public class TrackingController {

    private final TrackingRepository trackingRepository;

    public TrackingController(TrackingRepository trackingRepository) {
        this.trackingRepository = trackingRepository;
    }

    @GetMapping("/{shipmentId}")
    public ResponseEntity<TrackingRecord> getTracking(@PathVariable Long shipmentId) {
        TrackingRecord record = trackingRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No tracking data found"));
        return ResponseEntity.ok(record);
    }
}