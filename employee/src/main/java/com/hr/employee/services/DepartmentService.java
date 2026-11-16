package com.hr.employee.services;

import com.hr.employee.dtos.request.DepartmentRequestDto;
import com.hr.employee.dtos.response.DepartmentResponseDto;
import com.hr.employee.entities.Department;
import com.hr.employee.repositories.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository repository;

    @Transactional
    public DepartmentResponseDto create(DepartmentRequestDto req) {
        String code = req.code().trim().toUpperCase();
        String name = req.name().trim();

        if (repository.existsByCodeIgnoreCase(code))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department code already exists");
        if (repository.existsByNameIgnoreCase(name))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already exists");

        Department department = new Department();
        department.setCode(code);
        department.setName(name);
        return toResponse(repository.save(department));
    }

    public List<DepartmentResponseDto> list() {
        return repository.findAll().stream()
            .sorted(Comparator.comparing(Department::getName, String.CASE_INSENSITIVE_ORDER))
            .map(this::toResponse)
            .toList();
    }

    public DepartmentResponseDto getByCode(String code) {
        return toResponse(findByCode(code));
    }

    @Transactional
    public DepartmentResponseDto update(String code, DepartmentRequestDto req) {
        Department department = findByCode(code);
        String requestedCode = req.code().trim().toUpperCase();
        String name = req.name().trim();

        if (!department.getCode().equalsIgnoreCase(requestedCode))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department code cannot be changed");
        if (repository.existsByNameIgnoreCaseAndIdNot(name, department.getId()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department name already exists");

        department.setName(name);
        return toResponse(department);
    }

    @Transactional
    public void delete(String code) {
        Department department = findByCode(code);
        try {
            repository.delete(department);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Department is still used by employees");
        }
    }

    private Department findByCode(String code) {
        return repository.findByCodeIgnoreCase(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private DepartmentResponseDto toResponse(Department department) {
        return new DepartmentResponseDto(department.getCode(), department.getName());
    }
}
