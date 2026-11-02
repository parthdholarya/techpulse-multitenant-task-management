package com.techpulse.taskmanagement.controller;

import com.techpulse.taskmanagement.dto.TenantRegistrationRequest;
import com.techpulse.taskmanagement.dto.TenantRegistrationResponse;
import com.techpulse.taskmanagement.service.TenantService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/register")
    public ResponseEntity<TenantRegistrationResponse> registerTenant(
            @Valid @RequestBody TenantRegistrationRequest request) {

        TenantRegistrationResponse response =
                tenantService.registerTenant(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}