package com.hr.employee.dtos.response;
import com.hr.employee.enums.EmploymentStatusEnum;

import java.util.UUID;

public record EmployeeResponseDto(
    UUID id,
    String employeeCode,
    String fullName,
    String email,
    String department,
    EmploymentStatusEnum employmentStatus
) {}
