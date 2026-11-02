package com.techpulse.taskmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank
        String tenantName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password

) {}