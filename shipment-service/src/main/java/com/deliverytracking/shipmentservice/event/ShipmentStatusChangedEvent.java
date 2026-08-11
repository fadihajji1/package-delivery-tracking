package com.deliverytracking.shipmentservice.event;

import com.deliverytracking.shipmentservice.model.ShipmentStatus;
import java.time.LocalDateTime;

public record ShipmentStatusChangedEvent(
        Long shipmentId,
        ShipmentStatus oldStatus,
        ShipmentStatus newStatus,
        LocalDateTime changedAt
) {}