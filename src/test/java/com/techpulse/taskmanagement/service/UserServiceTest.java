
package com.techpulse.taskmanagement.service;

import com.techpulse.taskmanagement.dto.CreateUserRequest;
import com.techpulse.taskmanagement.dto.UserResponse;
import com.techpulse.taskmanagement.entity.Role;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.exception.DuplicateResourceException;
import com.techpulse.taskmanagement.exception.ResourceNotFoundException;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import com.techpulse.taskmanagement.security.TenantContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUserSuccessfully() {

        CreateUserRequest request = new CreateUserRequest(
                " USER@TECHPULSE.COM ",
                "password123",
                Role.USER
        );

        Tenant tenant = new Tenant();

        try (MockedStatic<TenantContext> mockedContext =
                     mockStatic(TenantContext.class)) {

            mockedContext.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            when(userRepository.existsByEmailAndTenant_Id(
                    " user@techpulse.com ", 1L))
                    .thenReturn(false);

            when(tenantRepository.findById(1L))
                    .thenReturn(Optional.of(tenant));

            when(passwordEncoder.encode("password123"))
                    .thenReturn("encoded-password");

            when(userRepository.save(any(User.class)))
                    .thenAnswer(invocation -> {
                        User user = invocation.getArgument(0);
                        user.setId(10L);
                        return user;
                    });

            UserResponse response = userService.createUser(request);

            assertNotNull(response);
            assertEquals(10L, response.id());
            assertEquals("user@techpulse.com", response.email());
            assertEquals("USER", response.role());
            assertEquals(1L, response.tenantId());
            assertEquals("User created successfully", response.message());

            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("password123");
        }
    }

    @Test
    void createUserShouldRejectAdminRole() {

        CreateUserRequest request = new CreateUserRequest(
                "admin@techpulse.com",
                "password123",
                Role.ADMIN
        );

        try (MockedStatic<TenantContext> mockedContext =
                     mockStatic(TenantContext.class)) {

            mockedContext.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.createUser(request)
            );

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    void createUserShouldRejectDuplicateEmail() {

        CreateUserRequest request = new CreateUserRequest(
                "user@techpulse.com",
                "password123",
                Role.USER
        );

        try (MockedStatic<TenantContext> mockedContext =
                     mockStatic(TenantContext.class)) {

            mockedContext.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            when(userRepository.existsByEmailAndTenant_Id(
                    "user@techpulse.com", 1L))
                    .thenReturn(true);

            assertThrows(
                    DuplicateResourceException.class,
                    () -> userService.createUser(request)
            );

            verify(tenantRepository, never()).findById(any());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Test
    void createUserShouldThrowExceptionWhenTenantNotFound() {

        CreateUserRequest request = new CreateUserRequest(
                "user@techpulse.com",
                "password123",
                Role.USER
        );

        try (MockedStatic<TenantContext> mockedContext =
                     mockStatic(TenantContext.class)) {

            mockedContext.when(TenantContext::getCurrentTenantId)
                    .thenReturn(1L);

            when(userRepository.existsByEmailAndTenant_Id(
                    "user@techpulse.com", 1L))
                    .thenReturn(false);

            when(tenantRepository.findById(1L))
                    .thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.createUser(request)
            );

            verify(userRepository, never()).save(any(User.class));
        }
    }
}
