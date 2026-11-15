package com.hr.employee.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
    @NotBlank(message = "Name cannot be empty")
    String name,

    @NotBlank(message = "Username cannot be empty")
    String username,

    @NotBlank(message = "Password cannot be empty")
    @Size(message = "Password should be at least 8 characters long", min = 8)
    String password
){}
