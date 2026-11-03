
package com.techpulse.taskmanagement.controller;

import com.techpulse.taskmanagement.dto.AssignTaskRequest;
import com.techpulse.taskmanagement.dto.CreateTaskRequest;
import com.techpulse.taskmanagement.dto.TaskResponse;
import com.techpulse.taskmanagement.dto.UpdateTaskStatusRequest;
import com.techpulse.taskmanagement.service.TaskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskControllerTest {

    private TaskService taskService;
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        taskController = new TaskController(taskService);
    }

    @Test
    void shouldCreateTaskSuccessfully() {

        CreateTaskRequest request = mock(CreateTaskRequest.class);
        TaskResponse expectedResponse = mock(TaskResponse.class);

        when(taskService.createTask(request))
                .thenReturn(expectedResponse);

        var response = taskController.createTask(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(taskService, times(1)).createTask(request);
    }

    @Test
    void shouldGetAllTasksSuccessfully() {

        List<TaskResponse> expectedTasks = List.of(
                mock(TaskResponse.class),
                mock(TaskResponse.class)
        );

        when(taskService.getTasks()).thenReturn(expectedTasks);

        var response = taskController.getTasks();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTasks, response.getBody());

        verify(taskService, times(1)).getTasks();
    }

    @Test
    void shouldAssignTaskSuccessfully() {

        Long taskId = 1L;

        AssignTaskRequest request = mock(AssignTaskRequest.class);
        TaskResponse expectedResponse = mock(TaskResponse.class);

        when(taskService.assignTask(taskId, request))
                .thenReturn(expectedResponse);

        var response = taskController.assignTask(taskId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(taskService, times(1)).assignTask(taskId, request);
    }

    @Test
    void shouldUpdateTaskStatusSuccessfully() {

        Long taskId = 1L;

        UpdateTaskStatusRequest request =
                mock(UpdateTaskStatusRequest.class);

        TaskResponse expectedResponse = mock(TaskResponse.class);

        when(taskService.updateTaskStatus(taskId, request))
                .thenReturn(expectedResponse);

        var response = taskController.updateTaskStatus(taskId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(taskService, times(1))
                .updateTaskStatus(taskId, request);
    }
}
