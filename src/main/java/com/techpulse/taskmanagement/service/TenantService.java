package com.techpulse.taskmanagement.service;
import com.techpulse.taskmanagement.dto.TenantRegistrationRequest;
import com.techpulse.taskmanagement.dto.TenantRegistrationResponse;
import com.techpulse.taskmanagement.entity.Role;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.exception.DuplicateResourceException;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public TenantRegistrationResponse registerTenant(
            TenantRegistrationRequest request) {

        String tenantName = request.tenantName().trim();
        String email = request.adminEmail().trim().toLowerCase();

        // Check duplicate tenant name
        if (tenantRepository.existsByName(tenantName)) {
            throw new DuplicateResourceException("Tenant already exists");
        }

        // Create tenant
        Tenant tenant = new Tenant();
        tenant.setName(tenantName);

        Tenant savedTenant = tenantRepository.save(tenant);

        // Create first ADMIN user
        User admin = new User();
        admin.setEmail(email);
        admin.setPassword(
                passwordEncoder.encode(request.adminPassword()));
        admin.setRole(Role.ADMIN);
        admin.setTenant(savedTenant);

        User savedAdmin = userRepository.save(admin);

        return new TenantRegistrationResponse(
                savedTenant.getId(),
                savedTenant.getName(),
                savedAdmin.getId(),
                savedAdmin.getEmail(),
                savedAdmin.getRole().name(),
                "Tenant registered successfully"
        );
    }
}
