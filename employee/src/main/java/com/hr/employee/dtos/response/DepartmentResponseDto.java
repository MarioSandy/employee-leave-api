package com.hr.employee.dtos.response;

import java.util.UUID;

public record DepartmentResponseDto(
    UUID id,
    String name
) { }
