package com.deliverytracking.trackingservice.kafka;

import java.time.LocalDateTime;

public record ShipmentStatusChangedEvent(
        Long shipmentId,
        Long customerId,
        String oldStatus,
        String newStatus,
        LocalDateTime changedAt
) {
}
