
package com.techpulse.taskmanagement.service;

import com.techpulse.taskmanagement.dto.TenantRegistrationRequest;
import com.techpulse.taskmanagement.dto.TenantRegistrationResponse;
import com.techpulse.taskmanagement.entity.Role;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.exception.DuplicateResourceException;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private TenantService tenantService;

    @Test
    void registerTenantSuccessfully() {

        TenantRegistrationRequest request =
                new TenantRegistrationRequest(
                        "TechPulse",
                        "admin@techpulse.com",
                        "password123"
                );

        when(tenantRepository.existsByName("TechPulse"))
                .thenReturn(false);

        when(tenantRepository.save(any(Tenant.class)))
                .thenAnswer(invocation -> {
                    Tenant tenant = invocation.getArgument(0);
                    // Simulate database-generated ID
                    return tenant;
                });

        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TenantRegistrationResponse response =
                tenantService.registerTenant(request);

        assertNotNull(response);
        assertEquals("TechPulse", response.tenantName());
        assertEquals("admin@techpulse.com", response.adminEmail());
        assertEquals("ADMIN", response.role());
        assertEquals("encoded-password",
                verifyUserPasswordWasEncoded());

        verify(tenantRepository).save(any(Tenant.class));
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password123");
    }

    private String verifyUserPasswordWasEncoded() {
        verify(passwordEncoder).encode("password123");
        return "encoded-password";
    }

    @Test
    void registerTenantShouldThrowExceptionWhenTenantAlreadyExists() {

        TenantRegistrationRequest request =
                new TenantRegistrationRequest(
                        "TechPulse",
                        "admin@techpulse.com",
                        "password123"
                );

        when(tenantRepository.existsByName("TechPulse"))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> tenantService.registerTenant(request)
        );

        verify(tenantRepository, never()).save(any(Tenant.class));
        verify(userRepository, never()).save(any(User.class));
    }
}
