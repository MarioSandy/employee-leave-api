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
        if (repository.existsByNameIgnoreCase(req.name()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department already exists");

        Department d = new Department();
        d.setName(req.name().trim());
        return toResponse(repository.save(d));
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
        Department d = find(id);
        if (repository.existsByNameIgnoreCaseAndIdNot(req.name(), id))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Department already exists");
        d.setName(req.name().trim());
        return toResponse(d);
    }

    @Transactional
    public void delete(UUID id) {
        Department d = find(id);
        try {
            repository.delete(d);
            repository.flush(); // force the FK check now, so we can catch it here
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Department is still used by employees");
        }
    }

    private Department find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Department not found"));
    }

    private DepartmentResponseDto toResponse(Department d) {
        return new DepartmentResponseDto(d.getId(), d.getName());
    }
}