package com.hr.employee.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmployeeRequestDto(
    @NotBlank @Size(max = 30) String employeeCode,
    @NotBlank @Size(max = 150) String fullName,
    @NotBlank @Email @Size(max = 150) String email,
    @NotBlank @Size(max = 100) String department
) {}