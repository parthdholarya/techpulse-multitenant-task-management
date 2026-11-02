package com.techpulse.taskmanagement.security;

public record TenantPrincipal(
        String email,
        Long tenantId,
        String role
) {}