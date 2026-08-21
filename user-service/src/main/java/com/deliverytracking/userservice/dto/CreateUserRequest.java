package com.deliverytracking.userservice.dto;

import com.deliverytracking.userservice.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        UserRole role,
        Boolean available
) {}