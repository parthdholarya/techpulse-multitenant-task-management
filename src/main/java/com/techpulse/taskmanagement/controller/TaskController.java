package com.techpulse.taskmanagement.controller;

import com.techpulse.taskmanagement.dto.AssignTaskRequest;
import com.techpulse.taskmanagement.dto.CreateTaskRequest;
import com.techpulse.taskmanagement.dto.TaskResponse;
import com.techpulse.taskmanagement.dto.UpdateTaskStatusRequest;
import com.techpulse.taskmanagement.service.TaskService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request) {

        TaskResponse response = taskService.createTask(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getTasks() {

        List<TaskResponse> tasks = taskService.getTasks();

        return ResponseEntity.ok(tasks);
    }

    @PutMapping("/{taskId}/assign")
    public ResponseEntity<TaskResponse> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskRequest request) {

        TaskResponse response = taskService.assignTask(taskId, request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request) {

        TaskResponse response =
                taskService.updateTaskStatus(taskId, request);

        return ResponseEntity.ok(response);
    }
}