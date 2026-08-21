package com.deliverytracking.deliveryservice.dto;

import com.deliverytracking.deliveryservice.model.DeliveryStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateDeliveryStatusRequest(@NotNull DeliveryStatus status) {
}
