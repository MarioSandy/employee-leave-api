package com.hr.leave.controllers;

import com.hr.leave.dtos.request.CreateLeaveRequestDto;
import com.hr.leave.dtos.request.LeaveReportEntry;
import com.hr.leave.dtos.request.RejectLeaveRequestDto;
import com.hr.leave.dtos.response.LeaveResponseDto;
import com.hr.leave.enums.LeaveStatusEnum;
import com.hr.leave.services.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()")
    public LeaveResponseDto submit(@Valid @RequestBody CreateLeaveRequestDto req,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.submit(req, employeeId(jwt), jwt.getTokenValue());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public List<LeaveResponseDto> mine(@AuthenticationPrincipal Jwt jwt) {
        return service.findMine(employeeId(jwt));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public List<LeaveResponseDto> list(@RequestParam(required = false) LeaveStatusEnum status) {
        return service.findAll(status);
    }

    @GetMapping("/report")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public List<LeaveReportEntry> report() {
        return service.report();
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public LeaveResponseDto approve(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.approve(id, employeeId(jwt));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
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