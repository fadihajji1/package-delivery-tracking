package com.deliverytracking.deliveryservice.client;

import java.time.LocalDateTime;

public record UserDto(
        Long id,
        String name,
        String email,
        String role,
        boolean available,
        LocalDateTime createdAt
) {
}
