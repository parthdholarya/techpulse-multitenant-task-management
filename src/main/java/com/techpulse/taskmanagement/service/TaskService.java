package com.techpulse.taskmanagement.service;

import com.techpulse.taskmanagement.dto.AssignTaskRequest;
import com.techpulse.taskmanagement.dto.CreateTaskRequest;
import com.techpulse.taskmanagement.dto.TaskResponse;
import com.techpulse.taskmanagement.dto.UpdateTaskStatusRequest;
import com.techpulse.taskmanagement.entity.Role;
import com.techpulse.taskmanagement.entity.Task;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.exception.ForbiddenOperationException;
import com.techpulse.taskmanagement.exception.ResourceNotFoundException;
import com.techpulse.taskmanagement.repository.TaskRepository;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import com.techpulse.taskmanagement.security.TenantContext;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public TaskService(
            TaskRepository taskRepository,
            TenantRepository tenantRepository,
            UserRepository userRepository) {

        this.taskRepository = taskRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    // Existing: Manager creates a task
    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {

        Long tenantId = TenantContext.getCurrentTenantId();

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setTenant(tenant);

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask, "Task created successfully");
    }

    // New: Get tasks according to logged-in user's role
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasks() {

        Long tenantId = TenantContext.getCurrentTenantId();
        String role = TenantContext.getCurrentRole();

        List<Task> tasks;

        if ("ADMIN".equals(role) || "MANAGER".equals(role)) {

            tasks = taskRepository.findAllByTenant_Id(tenantId);

        } else if ("USER".equals(role)) {

            String email = TenantContext.getCurrentEmail();

            User user = userRepository
                    .findByEmailAndTenant_Id(email, tenantId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            tasks = taskRepository
                    .findAllByTenant_IdAndAssignedTo_Id(tenantId, user.getId());

        } else {
            throw new RuntimeException("Invalid user role");
        }

        return tasks.stream()
                .map(task -> mapToResponse(task, "Task fetched successfully"))
                .toList();
    }

    private TaskResponse mapToResponse(Task task, String message) {

        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getTenant().getId(),
                message
        );
    }

    @Transactional
    public TaskResponse assignTask(Long taskId, AssignTaskRequest request) {

        Long tenantId = TenantContext.getCurrentTenantId();

        // Find task belonging to the manager's tenant
        Task task = taskRepository.findByIdAndTenant_Id(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Find user belonging to the same tenant
        User user = userRepository.findByIdAndTenant_Id(
                        request.userId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found in your tenant"));

        // Only assign tasks to MANAGER or USER? No!
        // A task should be assigned to a USER.
        if (user.getRole() != Role.USER) {
            throw new ForbiddenOperationException("Task can only be assigned to a USER");
        }

        task.setAssignedTo(user);

        Task savedTask = taskRepository.save(task);

        return mapToResponse(savedTask, "Task assigned successfully");
    }

    @Transactional
    public TaskResponse updateTaskStatus(
            Long taskId,
            UpdateTaskStatusRequest request) {

        Long tenantId = TenantContext.getCurrentTenantId();
        String email = TenantContext.getCurrentEmail();
        String role = TenantContext.getCurrentRole();

        // Only USER can update task status
        if (!"USER".equals(role)) {
            throw new ForbiddenOperationException(
                    "Only USER can update task status");
        }

        // Find logged-in user within their tenant
        User user = userRepository
                .findByEmailAndTenant_Id(email, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Find task within the same tenant
        Task task = taskRepository
                .findByIdAndTenant_Id(taskId, tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        // Verify task assignment
        if (task.getAssignedTo() == null ||
                !task.getAssignedTo().getId().equals(user.getId())) {

            throw new ForbiddenOperationException(
                    "You can update only your assigned tasks");
        }

        // Update status
        task.setStatus(request.status());

        Task updatedTask = taskRepository.save(task);

        return mapToResponse(
                updatedTask,
                "Task status updated successfully");
    }
}