package com.techpulse.taskmanagement.dto;

import com.techpulse.taskmanagement.entity.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Long tenantId,
        String message
) {}