package com.deliverytracking.deliveryservice.dto;

public record CreateDeliveryRequest(
        Long shipmentId,
        Long agentId
) {
}
