package com.hr.leave.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectLeaveRequestDto(
    @NotBlank @Size(max = 500) String reason
) { }
