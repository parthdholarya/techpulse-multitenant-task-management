
package com.techpulse.taskmanagement.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void shouldHandleGeneralException() {

        var response = handler.handleGeneralException(
                new RuntimeException("Something went wrong")
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Internal server error",
                response.getBody().get("error"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void shouldHandleResourceNotFoundException() {

        var response = handler.handleNotFoundException(
                new ResourceNotFoundException("Task not found")
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Task not found", response.getBody().get("error"));
    }

    @Test
    void shouldHandleDuplicateResourceException() {

        var response = handler.handleDuplicateException(
                new DuplicateResourceException("Email already exists")
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().get("status"));
        assertEquals("Email already exists",
                response.getBody().get("error"));
    }

    @Test
    void shouldHandleForbiddenOperationException() {

        var response = handler.handleForbiddenException(
                new ForbiddenOperationException("Access denied")
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals(403, response.getBody().get("status"));
        assertEquals("Access denied", response.getBody().get("error"));
    }

    @Test
    void shouldHandleAuthenticationFailedException() {

        var response = handler.handleAuthenticationFailed(
                new AuthenticationFailedException("Invalid credentials")
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals(401, response.getBody().get("status"));
        assertEquals("Invalid credentials",
                response.getBody().get("error"));
    }

    @Test
    void shouldHandleInvalidRequestBody() {

        var response = handler.handleInvalidRequest(
                new HttpMessageNotReadableException(
                        "Invalid enum",
                        new org.springframework.mock.http.MockHttpInputMessage(
                                new byte[0]
                        )
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Invalid request body or enum value",
                response.getBody().get("error"));
    }

    @Test
    void shouldHandleValidationException() {

        Object target = new Object();

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "request");

        bindingResult.addError(
                new FieldError(
                        "request",
                        "email",
                        target,
                        false,
                        null,
                        null,
                        "Email is required"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        var response = handler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));

        Map<?, ?> errors = (Map<?, ?>) response.getBody().get("errors");

        assertEquals("Email is required", errors.get("email"));
    }
}
