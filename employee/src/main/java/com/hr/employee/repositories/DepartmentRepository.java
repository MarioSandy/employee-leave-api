package com.hr.employee.repositories;

import com.hr.employee.entities.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    boolean existsByCodeIgnoreCase(String code);
    Optional<Department> findByCodeIgnoreCase(String code);
}
