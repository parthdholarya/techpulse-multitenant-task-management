package com.techpulse.taskmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TenantRegistrationRequest(

        @NotBlank(message = "Tenant name is required")
        @Size(max = 100, message = "Tenant name must not exceed 100 characters")
        String tenantName,

        @NotBlank(message = "Admin email is required")
        @Email(message = "Enter a valid email address")
        @Size(max = 254, message = "Email is too long")
        String adminEmail,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 72,
                message = "Password must contain between 8 and 72 characters")
        String adminPassword

) {
}
