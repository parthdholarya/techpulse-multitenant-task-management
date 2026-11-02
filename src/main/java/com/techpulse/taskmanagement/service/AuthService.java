package com.techpulse.taskmanagement.service;

import com.techpulse.taskmanagement.dto.LoginRequest;
import com.techpulse.taskmanagement.dto.LoginResponse;
import com.techpulse.taskmanagement.entity.Tenant;
import com.techpulse.taskmanagement.entity.User;
import com.techpulse.taskmanagement.exception.AuthenticationFailedException;
import com.techpulse.taskmanagement.repository.TenantRepository;
import com.techpulse.taskmanagement.repository.UserRepository;
import com.techpulse.taskmanagement.security.JwtService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        Tenant tenant = tenantRepository
                .findByName(request.tenantName().trim())
                .orElseThrow(() ->
                        new AuthenticationFailedException("Invalid credentials"));

        String email = request.email().trim().toLowerCase();

        User user = userRepository
                .findByEmailAndTenant_Id(email, tenant.getId())
                .orElseThrow(() ->
                        new AuthenticationFailedException("Invalid credentials"));

        System.out.println("Tenant found: " + tenant.getName());
        System.out.println("User found: " + user.getEmail());
        System.out.println("Password matches: " +
                passwordEncoder.matches(
                        request.password(), user.getPassword()));
        if (!passwordEncoder.matches(
                request.password(), user.getPassword())) {

            throw new AuthenticationFailedException("Invalid credentials");
        }

        String token = jwtService.generateToken(
                user.getEmail(),
                tenant.getId(),
                user.getRole().name()
        );

        return new LoginResponse(
                token,
                "Bearer",
                tenant.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}