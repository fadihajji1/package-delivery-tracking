package com.deliverytracking.notificationservice.kafka;

import java.time.LocalDateTime;

public record DeliveryAssignedEvent(
        Long shipmentId,
        Long customerId,
        Long agentId,
        LocalDateTime assignedAt
) {
}
