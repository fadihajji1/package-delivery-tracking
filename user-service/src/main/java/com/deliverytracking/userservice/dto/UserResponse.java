package com.deliverytracking.userservice.dto;

import com.deliverytracking.userservice.model.UserRole;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        boolean available,
        LocalDateTime createdAt
) {}