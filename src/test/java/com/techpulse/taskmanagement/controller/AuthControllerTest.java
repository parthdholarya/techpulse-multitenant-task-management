
package com.techpulse.taskmanagement.controller;

import com.techpulse.taskmanagement.dto.LoginRequest;
import com.techpulse.taskmanagement.dto.LoginResponse;
import com.techpulse.taskmanagement.service.AuthService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequest request = mock(LoginRequest.class);
        LoginResponse expectedResponse = mock(LoginResponse.class);

        when(authService.login(request))
                .thenReturn(expectedResponse);

        var response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertSame(expectedResponse, response.getBody());

        verify(authService, times(1)).login(request);
    }
}
