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

    @GetMapping("/{code}")
    @RequireRole({UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public DepartmentResponseDto get(@PathVariable String code) {
        return service.getByCode(code);
    }

    @PutMapping("/{code}")
    @RequireRole({UserRoleEnum.ADMIN})
    public DepartmentResponseDto update(@PathVariable String code, @Valid @RequestBody DepartmentRequestDto req) {
        return service.update(code, req);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequireRole({UserRoleEnum.ADMIN})
    public void delete(@PathVariable String code) {
        service.delete(code);
    }
}
