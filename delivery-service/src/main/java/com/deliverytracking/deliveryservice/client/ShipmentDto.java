package com.deliverytracking.deliveryservice.client;

import java.time.LocalDateTime;

public record ShipmentDto(
	Long id,
	Long customerId,
	String status,
	String origin,
	String destination,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}