package com.techpulse.taskmanagement.dto;

public record UserResponse(
        Long id,
        String email,
        String role,
        Long tenantId,
        String message
) {}