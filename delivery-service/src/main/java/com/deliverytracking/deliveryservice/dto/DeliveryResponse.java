package com.deliverytracking.deliveryservice.dto;

import com.deliverytracking.deliveryservice.model.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliveryResponse(
        Long id,
        Long shipmentId,
        Long agentId,
        LocalDateTime assignedAt,
        DeliveryStatus status
) {
}
