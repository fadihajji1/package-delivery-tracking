package com.deliverytracking.shipmentservice.dto;

import com.deliverytracking.shipmentservice.model.ShipmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateShipmentStatusRequest(@NotNull ShipmentStatus status) {
}
