
package com.techpulse.taskmanagement.service;

import com.techpulse.taskmanagement.dto.CreateTaskRequest;
import com.techpulse.taskmanagement.dto.TaskResponse;
import com.techpulse.taskmanagement.entity.Task;
import com.techpulse.taskmanagement.entity.TaskStatus;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.exception.ResourceNotFoundException;
import com.techpulse.taskmanagement.repository.TaskRepository;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import com.techpulse.taskmanagement.security.TenantContext;
import com.techpulse.taskmanagement.dto.AssignTaskRequest;
import com.techpulse.taskmanagement.entity.Role;
import com.techpulse.taskmanagement.exception.ForbiddenOperationException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.techpulse.taskmanagement.dto.UpdateTaskStatusRequest;
import com.techpulse.taskmanagement.exception.ForbiddenOperationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;


    // 1. Successfully create a task
    @Test
    void createTaskSuccessfully() {

        CreateTaskRequest request = new CreateTaskRequest(
                "Implement JWT Authentication",
                "Implement JWT security in Spring Boot"
        );

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            when(tenantRepository.findById(1L))
                    .thenReturn(Optional.of(tenant));

            when(taskRepository.save(any(Task.class)))
                    .thenAnswer(invocation -> {
                        Task task = invocation.getArgument(0);
                        task.setId(100L);
                        return task;
                    });

            TaskResponse response = taskService.createTask(request);

            assertNotNull(response);
            assertEquals(100L, response.id());
            assertEquals("Implement JWT Authentication", response.title());
            assertEquals(
                    "Implement JWT security in Spring Boot",
                    response.description()
            );
            assertEquals(TaskStatus.PENDING, response.status());
            assertEquals(1L, response.tenantId());
            assertEquals("Task created successfully", response.message());

            verify(tenantRepository).findById(1L);
            verify(taskRepository).save(any(Task.class));
        }
    }


    // 2. Tenant not found while creating task
    @Test
    void createTaskShouldThrowExceptionWhenTenantNotFound() {

        CreateTaskRequest request = new CreateTaskRequest(
                "Test Task",
                "Test Description"
        );

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            when(tenantRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> taskService.createTask(request)
            );

            verify(taskRepository, never()).save(any(Task.class));
        }
    }


    // 3. ADMIN can see all tasks belonging to their tenant
    @Test
    void adminShouldSeeAllTasksWithinTenant() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setTenant(tenant);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setTenant(tenant);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            context.when(TenantContext::getCurrentRole)
                    .thenReturn("ADMIN");

            when(taskRepository.findAllByTenant_Id(1L))
                    .thenReturn(List.of(task1, task2));

            List<TaskResponse> responses = taskService.getTasks();

            assertEquals(2, responses.size());
            assertEquals("Task 1", responses.get(0).title());
            assertEquals("Task 2", responses.get(1).title());

            verify(taskRepository).findAllByTenant_Id(1L);

            verify(userRepository, never())
                    .findByEmailAndTenant_Id(anyString(), anyLong());
        }
    }


    // 4. USER can see only assigned tasks
    @Test
    void userShouldSeeOnlyAssignedTasks() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        User user = new User();
        user.setId(10L);
        user.setEmail("user@techpulse.com");
        user.setTenant(tenant);

        Task assignedTask = new Task();
        assignedTask.setId(1L);
        assignedTask.setTitle("My Task");
        assignedTask.setTenant(tenant);
        assignedTask.setAssignedTo(user);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            context.when(TenantContext::getCurrentRole)
                    .thenReturn("USER");

            context.when(TenantContext::getCurrentEmail)
                    .thenReturn("user@techpulse.com");

            when(userRepository.findByEmailAndTenant_Id(
                    "user@techpulse.com", 1L))
                    .thenReturn(Optional.of(user));

            when(taskRepository.findAllByTenant_IdAndAssignedTo_Id(1L, 10L))
                    .thenReturn(List.of(assignedTask));

            List<TaskResponse> responses = taskService.getTasks();

            assertEquals(1, responses.size());
            assertEquals("My Task", responses.get(0).title());

            verify(taskRepository)
                    .findAllByTenant_IdAndAssignedTo_Id(1L, 10L);

            verify(taskRepository, never())
                    .findAllByTenant_Id(1L);
        }
    }


    // 5. MANAGER can see all tasks belonging to their tenant
    @Test
    void managerShouldSeeAllTasksWithinTenant() {

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            context.when(TenantContext::getCurrentRole)
                    .thenReturn("MANAGER");

            when(taskRepository.findAllByTenant_Id(1L))
                    .thenReturn(List.of());

            List<TaskResponse> responses = taskService.getTasks();

            assertTrue(responses.isEmpty());

            verify(taskRepository).findAllByTenant_Id(1L);
        }
    }


    @Test
    void assignTaskSuccessfully() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        User user = new User();
        user.setId(10L);
        user.setRole(Role.USER);
        user.setTenant(tenant);

        Task task = new Task();
        task.setId(100L);
        task.setTitle("Implement JWT");
        task.setTenant(tenant);

        AssignTaskRequest request = new AssignTaskRequest(10L);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            when(taskRepository.findByIdAndTenant_Id(100L, 1L))
                    .thenReturn(Optional.of(task));

            when(userRepository.findByIdAndTenant_Id(10L, 1L))
                    .thenReturn(Optional.of(user));

            when(taskRepository.save(any(Task.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TaskResponse response = taskService.assignTask(100L, request);

            assertEquals("Task assigned successfully", response.message());
            assertEquals(10L, task.getAssignedTo().getId());

            verify(taskRepository).save(task);
        }
    }

    @Test
    void assignTaskShouldRejectTaskFromAnotherTenant() {

        AssignTaskRequest request = new AssignTaskRequest(10L);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            when(taskRepository.findByIdAndTenant_Id(100L, 1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> taskService.assignTask(100L, request)
            );

            verify(userRepository, never())
                    .findByIdAndTenant_Id(anyLong(), anyLong());

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Test
    void assignTaskShouldRejectAdminUser() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        User admin = new User();
        admin.setId(10L);
        admin.setRole(Role.ADMIN);

        Task task = new Task();
        task.setId(100L);
        task.setTenant(tenant);

        AssignTaskRequest request = new AssignTaskRequest(10L);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            when(taskRepository.findByIdAndTenant_Id(100L, 1L))
                    .thenReturn(Optional.of(task));

            when(userRepository.findByIdAndTenant_Id(10L, 1L))
                    .thenReturn(Optional.of(admin));

            assertThrows(
                    ForbiddenOperationException.class,
                    () -> taskService.assignTask(100L, request)
            );

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Test
    void assignTaskShouldRejectUserFromAnotherTenant() {

        AssignTaskRequest request = new AssignTaskRequest(99L);

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        Task task = new Task();
        task.setId(100L);
        task.setTenant(tenant);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            when(taskRepository.findByIdAndTenant_Id(100L, 1L))
                    .thenReturn(Optional.of(task));

            when(userRepository.findByIdAndTenant_Id(99L, 1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    RuntimeException.class,
                    () -> taskService.assignTask(
                            100L, request
                    )
            );

            verify(taskRepository, never()).save(any(Task.class));
        }
    }


    @Test
    void userShouldUpdateAssignedTaskStatusSuccessfully() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        User user = new User();
        user.setId(10L);
        user.setEmail("user@techpulse.com");
        user.setTenant(tenant);

        Task task = new Task();
        task.setId(100L);
        task.setTitle("Implement JWT");
        task.setTenant(tenant);
        task.setAssignedTo(user);

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(TaskStatus.COMPLETED);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId).thenReturn(1L);
            context.when(TenantContext::getCurrentEmail)
                    .thenReturn("user@techpulse.com");
            context.when(TenantContext::getCurrentRole).thenReturn("USER");

            when(userRepository.findByEmailAndTenant_Id(
                    "user@techpulse.com", 1L))
                    .thenReturn(Optional.of(user));

            when(taskRepository.findByIdAndTenant_Id(100L, 1L))
                    .thenReturn(Optional.of(task));

            when(taskRepository.save(any(Task.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            TaskResponse response =
                    taskService.updateTaskStatus(100L, request);

            assertEquals(TaskStatus.COMPLETED, response.status());
            assertEquals(
                    "Task status updated successfully",
                    response.message()
            );

            verify(taskRepository).save(task);
        }
    }

    @Test
    void userShouldNotUpdateAnotherUsersTask() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        User loggedInUser = new User();
        loggedInUser.setId(10L);

        User assignedUser = new User();
        assignedUser.setId(20L);

        Task task = new Task();
        task.setId(100L);
        task.setTenant(tenant);
        task.setAssignedTo(assignedUser);

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(TaskStatus.COMPLETED);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId).thenReturn(1L);
            context.when(TenantContext::getCurrentEmail)
                    .thenReturn("user@techpulse.com");
            context.when(TenantContext::getCurrentRole).thenReturn("USER");

            when(userRepository.findByEmailAndTenant_Id(
                    "user@techpulse.com", 1L))
                    .thenReturn(Optional.of(loggedInUser));

            when(taskRepository.findByIdAndTenant_Id(100L, 1L))
                    .thenReturn(Optional.of(task));

            assertThrows(
                    ForbiddenOperationException.class,
                    () -> taskService.updateTaskStatus(100L, request)
            );

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Test
    void managerShouldNotUpdateTaskStatus() {

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(TaskStatus.COMPLETED);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId).thenReturn(1L);
            context.when(TenantContext::getCurrentEmail)
                    .thenReturn("manager@techpulse.com");
            context.when(TenantContext::getCurrentRole).thenReturn("MANAGER");

            assertThrows(
                    ForbiddenOperationException.class,
                    () -> taskService.updateTaskStatus(100L, request)
            );

            verify(userRepository, never())
                    .findByEmailAndTenant_Id(anyString(), anyLong());

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Test
    void userShouldNotUpdateTaskFromAnotherTenant() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        User user = new User();
        user.setId(10L);

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(TaskStatus.COMPLETED);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId).thenReturn(1L);
            context.when(TenantContext::getCurrentEmail)
                    .thenReturn("user@techpulse.com");
            context.when(TenantContext::getCurrentRole).thenReturn("USER");

            when(userRepository.findByEmailAndTenant_Id(
                    "user@techpulse.com", 1L))
                    .thenReturn(Optional.of(user));

            // Task does not exist within the authenticated tenant.
            when(taskRepository.findByIdAndTenant_Id(100L, 1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> taskService.updateTaskStatus(100L, request)
            );

            verify(taskRepository, never()).save(any(Task.class));
        }
    }

    @Test
    void userShouldNotUpdateUnassignedTask() {

        Tenant tenant = new Tenant();
        tenant.setId(1L);

        User user = new User();
        user.setId(10L);

        Task task = new Task();
        task.setId(100L);
        task.setTenant(tenant);
        task.setAssignedTo(null);

        UpdateTaskStatusRequest request =
                new UpdateTaskStatusRequest(TaskStatus.IN_PROGRESS);

        try (MockedStatic<TenantContext> context =
                     mockStatic(TenantContext.class)) {

            context.when(TenantContext::getCurrentTenantId).thenReturn(1L);
            context.when(TenantContext::getCurrentEmail)
                    .thenReturn("user@techpulse.com");
            context.when(TenantContext::getCurrentRole).thenReturn("USER");

            when(userRepository.findByEmailAndTenant_Id(
                    "user@techpulse.com", 1L))
                    .thenReturn(Optional.of(user));

            when(taskRepository.findByIdAndTenant_Id(100L, 1L))
                    .thenReturn(Optional.of(task));

            assertThrows(
                    ForbiddenOperationException.class,
                    () -> taskService.updateTaskStatus(100L, request)
            );

            verify(taskRepository, never()).save(any(Task.class));
        }
    }


}
