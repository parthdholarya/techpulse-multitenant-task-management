
package com.techpulse.taskmanagement.controller;

import com.techpulse.taskmanagement.dto.CreateUserRequest;
import com.techpulse.taskmanagement.dto.UserResponse;
import com.techpulse.taskmanagement.service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void shouldCreateUserSuccessfully() {

        CreateUserRequest request = mock(CreateUserRequest.class);
        UserResponse expectedResponse = mock(UserResponse.class);

        when(userService.createUser(request))
                .thenReturn(expectedResponse);

        var response = userController.createUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(userService, times(1)).createUser(request);
    }
}
