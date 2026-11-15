package com.hr.employee.services;

import com.hr.employee.dtos.request.EmployeeRequestDto;
import com.hr.employee.dtos.response.EmployeeResponseDto;
import com.hr.employee.entities.Employee;
import com.hr.employee.enums.EmploymentStatusEnum;
import com.hr.employee.repositories.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository repository;

    @Transactional
    public EmployeeResponseDto create(EmployeeRequestDto req) {
        if (repository.existsByEmployeeCode(req.employeeCode()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists");
        if (repository.existsByEmail(req.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        Employee e = new Employee();
        apply(e, req);

        Employee savedEmployee = repository.save(e);
        return new EmployeeResponseDto(
            savedEmployee.getId(),
            savedEmployee.getEmployeeCode(),
            savedEmployee.getFullName(),
            savedEmployee.getEmail(),
            savedEmployee.getDepartment(),
            savedEmployee.getEmploymentStatus()
        );
    }

    public EmployeeResponseDto getById(UUID id) {
        Employee employee = find(id);
        return new EmployeeResponseDto(
            employee.getId(),
            employee.getEmployeeCode(),
            employee.getFullName(),
            employee.getEmail(),
            employee.getDepartment(),
            employee.getEmploymentStatus()
        );
    }

    public List<EmployeeResponseDto> list(String department) {
        return repository.findAll().stream()
                .filter(e -> department == null || e.getDepartment().equalsIgnoreCase(department))
                .map(e -> new EmployeeResponseDto(
                    e.getId(),
                    e.getEmployeeCode(),
                    e.getFullName(),
                    e.getEmail(),
                    e.getDepartment(),
                    e.getEmploymentStatus()
                ))
                .toList();
    }

    @Transactional
    public EmployeeResponseDto update(UUID id, EmployeeRequestDto req) {
        Employee e = find(id);
        apply(e, req);
        return new EmployeeResponseDto(
            e.getId(),
            e.getEmployeeCode(),
            e.getFullName(),
            e.getEmail(),
            e.getDepartment(),
            e.getEmploymentStatus()
        );
    }

    @Transactional
    public void delete(UUID id) {
        Employee e = find(id);
        e.setEmploymentStatus(EmploymentStatusEnum.INACTIVE);
        e.setDeletedAt(Instant.now());
    }

    public Map<String, Long> countByDepartment() {
        return repository.findAll().stream()
                .filter(e -> e.getEmploymentStatus() == EmploymentStatusEnum.ACTIVE)
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new, Collectors.counting()));
    }

    private Employee find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private void apply(Employee e, EmployeeRequestDto req) {
        e.setEmployeeCode(req.employeeCode());
        e.setFullName(req.fullName());
        e.setEmail(req.email());
        e.setDepartment(req.department());
    }
}