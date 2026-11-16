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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository repository;

    @Transactional
    public DepartmentResponseDto create(DepartmentRequestDto req) {
        String name = req.name().trim();
        if (repository.existsByNameIgnoreCase(name))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department already exists");

        Department department = new Department();
        department.setName(name);
        return toResponse(repository.save(department));
    }

    public List<DepartmentResponseDto> list() {
        return repository.findAll().stream()
            .sorted(Comparator.comparing(Department::getName, String.CASE_INSENSITIVE_ORDER))
            .map(this::toResponse)
            .toList();
    }

    public DepartmentResponseDto getById(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public DepartmentResponseDto update(UUID id, DepartmentRequestDto req) {
        Department department = find(id);
        String name = req.name().trim();

        if (repository.existsByNameIgnoreCaseAndIdNot(name, id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department already exists");

        department.setName(name);
        return toResponse(department);
    }

    @Transactional
    public void delete(UUID id) {
        Department department = find(id);
        try {
            repository.delete(department);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Department is still used by employees");
        }
    }

    private Department find(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private DepartmentResponseDto toResponse(Department department) {
        return new DepartmentResponseDto(department.getId(), department.getName());
    }
}