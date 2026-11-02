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

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {

        // Get tenant ID from authenticated JWT context
        Long tenantId = TenantContext.getCurrentTenantId();

        // Only MANAGER and USER can be created through this API
        if (request.role() == Role.ADMIN) {
            throw new IllegalArgumentException(
                    "Cannot create another ADMIN");
        }

        String email = request.email().trim().toLowerCase();

        // Check duplicate email within the same tenant
        if (userRepository.existsByEmailAndTenant_Id(
                request.email().toLowerCase(), tenantId)) {

            throw new DuplicateResourceException(
                    "Email already exists in this tenant");
        }

        // Find tenant using authenticated tenant ID
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setTenant(tenant);

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                tenantId,
                "User created successfully"
        );
    }
}