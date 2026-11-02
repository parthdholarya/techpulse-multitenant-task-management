
package com.techpulse.taskmanagement.dto;

public record TenantRegistrationResponse(
        Long tenantId,
        String tenantName,
        Long adminUserId,
        String adminEmail,
        String role,
        String message
) {
}
