package com.deliverytracking.shipmentservice.event;

import java.time.LocalDateTime;

public record ShipmentCreatedEvent(
        Long shipmentId,
        Long customerId,
        String origin,
        String destination,
        LocalDateTime createdAt
) {}