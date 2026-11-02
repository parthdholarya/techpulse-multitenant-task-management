package com.techpulse.taskmanagement.dto;

import com.techpulse.taskmanagement.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTaskStatusRequest(

        @NotNull
        TaskStatus status

) {
}