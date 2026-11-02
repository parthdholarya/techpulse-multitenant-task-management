package com.techpulse.taskmanagement.repository;

import com.techpulse.taskmanagement.entity.Tenant;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface TenantRepository extends Repository<Tenant, Long> {

    Tenant save(Tenant tenant);

    Optional<Tenant> findByName(String name);

    boolean existsByName(String name);

    Optional<Tenant> findById(Long id);
}

