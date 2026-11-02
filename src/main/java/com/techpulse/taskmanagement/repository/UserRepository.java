package com.techpulse.taskmanagement.repository;

import com.techpulse.taskmanagement.entity.User;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {

    // Save a user
    User save(User user);

    // Find user inside a particular tenant
    Optional<User> findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );

    // Find user by email inside a particular tenant
    Optional<User> findByEmailAndTenant_Id(
            String email,
            Long tenantId
    );

    // Check duplicate email inside a tenant
    boolean existsByEmailAndTenant_Id(
            String email,
            Long tenantId
    );
    Optional<User> findFirstByEmail(String email);
}
