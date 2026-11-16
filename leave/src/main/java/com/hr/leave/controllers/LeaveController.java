package com.hr.leave.controllers;

import com.hr.leave.dtos.request.CreateLeaveRequestDto;
import com.hr.leave.dtos.request.LeaveReportEntry;
import com.hr.leave.dtos.request.RejectLeaveRequestDto;
import com.hr.leave.dtos.response.LeaveResponseDto;
import com.hr.leave.annotations.RequireRole;
import com.hr.leave.enums.LeaveStatusEnum;
import com.hr.leave.enums.UserRoleEnum;
import com.hr.leave.services.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/leave-requests")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequireRole({UserRoleEnum.EMPLOYEE, UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public LeaveResponseDto submit(@Valid @RequestBody CreateLeaveRequestDto req,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.submit(req, employeeId(jwt), jwt.getTokenValue());
    }

    @GetMapping("/me")
    @RequireRole({UserRoleEnum.EMPLOYEE, UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public List<LeaveResponseDto> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.findMine(employeeId(jwt));
    }

    @GetMapping
    @RequireRole({UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public List<LeaveResponseDto> list(@RequestParam(required = false) LeaveStatusEnum status) {
        return service.findAll(status);
    }

    @GetMapping("/report")
    @RequireRole({UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public List<LeaveReportEntry> report() {
        return service.report();
    }

    @PatchMapping("/{id}/approve")
    @RequireRole({UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public LeaveResponseDto approve(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.approve(id, employeeId(jwt));
    }

    @PatchMapping("/{id}/reject")
    @RequireRole({UserRoleEnum.MANAGER, UserRoleEnum.ADMIN})
    public LeaveResponseDto reject(
        @PathVariable UUID id,
        @Valid @RequestBody RejectLeaveRequestDto req,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return service.reject(id, employeeId(jwt), req.reason());
    }

    private UUID employeeId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("employeeId"));
    }
}