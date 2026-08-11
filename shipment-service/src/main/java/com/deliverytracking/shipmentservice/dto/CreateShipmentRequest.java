package com.deliverytracking.shipmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateShipmentRequest(
        @NotNull Long customerId,
        @NotBlank String origin,
        @NotBlank String destination
) {}