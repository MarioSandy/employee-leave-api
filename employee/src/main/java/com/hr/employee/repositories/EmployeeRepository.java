package com.hr.employee.repositories;

import com.hr.employee.entities.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
    boolean existsByEmployeeCodeAndIdNot(String employeeCode, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);
    Optional<Employee> findByEmployeeCode(String employeeCode);
}