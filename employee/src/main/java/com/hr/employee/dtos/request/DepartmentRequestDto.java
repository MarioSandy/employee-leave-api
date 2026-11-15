package com.hr.employee.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequestDto(
    @NotBlank @Size(max = 100) String name
) {}