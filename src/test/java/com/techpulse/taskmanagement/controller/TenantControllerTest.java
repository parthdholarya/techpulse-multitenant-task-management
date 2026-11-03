
package com.techpulse.taskmanagement.controller;

import com.techpulse.taskmanagement.dto.TenantRegistrationRequest;
import com.techpulse.taskmanagement.dto.TenantRegistrationResponse;
import com.techpulse.taskmanagement.service.TenantService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantControllerTest {

    private TenantService tenantService;
    private TenantController tenantController;

    @BeforeEach
    void setUp() {
        tenantService = mock(TenantService.class);
        tenantController = new TenantController(tenantService);
    }

    @Test
    void shouldRegisterTenantSuccessfully() {

        TenantRegistrationRequest request =
                new TenantRegistrationRequest(
                        "TechPulse",
                        "admin@techpulse.com",
                        "Password@123"
                );

        TenantRegistrationResponse expectedResponse = null;

        when(tenantService.registerTenant(request))
                .thenReturn(expectedResponse);

        var response = tenantController.registerTenant(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNull(response.getBody());

        verify(tenantService, times(1)).registerTenant(request);
    }
}
