package com.hr.leave.dtos.request;

import com.hr.leave.enums.LeaveTypeEnum;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateLeaveRequestDto(
    @NotNull LeaveTypeEnum leaveType,
    @NotNull @FutureOrPresent LocalDate startDate,
    @NotNull LocalDate endDate,
    @Size(max = 500) String reason,
    @Size(max = 500) String attachmentUrl
) {}
