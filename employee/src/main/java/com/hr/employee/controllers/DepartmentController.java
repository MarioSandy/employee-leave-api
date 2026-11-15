package com.hr.employee.controllers;

import com.hr.employee.annotations.RequireRole;
import com.hr.employee.dtos.request.DepartmentRequestDto;
import com.hr.employee.dtos.response.DepartmentResponseDto;
import com.hr.employee.enums.UserRoleEnum;
import com.hr.employee.services.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole({UserRoleEnum.ADMIN})
    public DepartmentResponseDto create(@Valid @RequestBody DepartmentRequestDto req) {
        return service.create(req);
    }

    @GetMapping
    @RequireRole({UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public List<DepartmentResponseDto> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    @RequireRole({UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public DepartmentResponseDto get(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    public DepartmentResponseDto update(@PathVariable UUID id, @Valid @RequestBody DepartmentRequestDto req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequireRole({UserRoleEnum.ADMIN})
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}