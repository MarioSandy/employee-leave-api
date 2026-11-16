package com.hr.employee.services;

import com.hr.employee.dtos.request.EmployeeRequestDto;
import com.hr.employee.dtos.response.EmployeeResponseDto;
import com.hr.employee.entities.Department;
import com.hr.employee.entities.Employee;
import com.hr.employee.enums.EmploymentStatusEnum;
import com.hr.employee.repositories.DepartmentRepository;
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
    private final DepartmentRepository departmentRepository;

    @Transactional
    public EmployeeResponseDto create(EmployeeRequestDto req) {
        if (repository.existsByEmployeeCode(req.employeeCode()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists");
        if (repository.existsByEmail(req.email()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        Employee employee = new Employee();
        apply(employee, req);
        return toResponse(repository.save(employee));
    }

    public EmployeeResponseDto getById(UUID id) {
        return toResponse(find(id));
    }

    public List<EmployeeResponseDto> list(String department) {
        return repository.findAll().stream()
            .filter(e -> department == null
                || e.getDepartment().getName().equalsIgnoreCase(department))
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public EmployeeResponseDto update(UUID id, EmployeeRequestDto req) {
        Employee employee = find(id);

        if (repository.existsByEmployeeCodeAndIdNot(req.employeeCode(), id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Employee code already exists");
        if (repository.existsByEmailAndIdNot(req.email(), id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");

        apply(employee, req);
        return toResponse(employee);
    }

    @Transactional
    public void delete(UUID id) {
        Employee employee = find(id);
        employee.setEmploymentStatus(EmploymentStatusEnum.INACTIVE);
        employee.setDeletedAt(Instant.now());
    }

    public Map<String, Long> countByDepartment() {
        return repository.findAll().stream()
            .filter(e -> e.getEmploymentStatus() == EmploymentStatusEnum.ACTIVE)
            .collect(Collectors.groupingBy(
                e -> e.getDepartment().getName(),
                TreeMap::new,
                Collectors.counting()
            ));
    }

    private Employee find(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private void apply(Employee employee, EmployeeRequestDto req) {
        Department department = departmentRepository.findById(req.departmentId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));

        employee.setEmployeeCode(req.employeeCode());
        employee.setFullName(req.fullName());
        employee.setEmail(req.email());
        employee.setDepartment(department);
    }

    private EmployeeResponseDto toResponse(Employee employee) {
        return new EmployeeResponseDto(
            employee.getId(),
            employee.getEmployeeCode(),
            employee.getFullName(),
            employee.getEmail(),
            employee.getDepartment().getName(),
            employee.getEmploymentStatus()
        );
    }
}