package com.deliverytracking.shipmentservice.dto;

import com.deliverytracking.shipmentservice.model.ShipmentStatus;
import java.time.LocalDateTime;

public record ShipmentResponse(
        Long id,
        Long customerId,
        ShipmentStatus status,
        String origin,
        String destination,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}