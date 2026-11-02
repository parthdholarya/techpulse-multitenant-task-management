package com.techpulse.taskmanagement.dto;

public record LoginResponse(
        String token,
        String tokenType,
        Long tenantId,
        String email,
        String role
) {}