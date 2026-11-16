package com.hr.employee.controllers;

import com.hr.employee.annotations.RequireRole;
import com.hr.employee.dtos.request.EmployeeRequestDto;
import com.hr.employee.dtos.response.EmployeeResponseDto;
import com.hr.employee.enums.UserRoleEnum;
import com.hr.employee.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService service;

    @PostMapping
    @RequireRole({UserRoleEnum.ADMIN})
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponseDto create(@Valid @RequestBody EmployeeRequestDto req) {
        return service.create(req);
    }

    @GetMapping
    @RequireRole({UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public List<EmployeeResponseDto> list(@RequestParam(required = false) String department) {
        return service.list(department);
    }

    @GetMapping("/summary/by-department")
    @RequireRole({UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public Map<String, Long> summary() {
        return service.countByDepartment();
    }

    @GetMapping("/{id}")
    public EmployeeResponseDto get(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        String role = jwt.getClaimAsString("role");
        boolean privileged = UserRoleEnum.ADMIN.name().equals(role)
            || UserRoleEnum.MANAGER.name().equals(role);
        String employeeId = jwt.getClaimAsString("employeeId");

        if (!privileged && (employeeId == null || !id.equals(UUID.fromString(employeeId)))) {
            throw new AccessDeniedException("You can only view your own employee record.");
        }

        return service.getById(id);
    }

    @PutMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    public EmployeeResponseDto update(@PathVariable UUID id, @Valid @RequestBody EmployeeRequestDto req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}