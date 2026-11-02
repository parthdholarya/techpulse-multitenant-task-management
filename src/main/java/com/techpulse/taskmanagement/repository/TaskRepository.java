package com.techpulse.taskmanagement.repository;

import com.techpulse.taskmanagement.entity.Task;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends Repository<Task, Long> {

    // Save a new or updated task
    Task save(Task task);

    // Find one task belonging to a tenant
    Optional<Task> findByIdAndTenant_Id(
            Long id,
            Long tenantId
    );

    // Get all tasks belonging to a tenant
    List<Task> findAllByTenant_Id(
            Long tenantId
    );

    // Get only tasks assigned to a particular user
    List<Task> findAllByTenant_IdAndAssignedTo_Id(
            Long tenantId,
            Long userId
    );
}
