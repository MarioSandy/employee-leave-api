package com.hr.leave.dtos.response;

import com.hr.leave.enums.LeaveStatusEnum;
import com.hr.leave.enums.LeaveTypeEnum;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LeaveResponseDto(
    UUID id,
    UUID employeeId,
    LeaveTypeEnum leaveType,
    LocalDate startDate,
    LocalDate endDate,
    long totalDays,
    String reason,
    String attachmentUrl,
    LeaveStatusEnum status,
    UUID reviewedBy,
    Instant reviewedAt,
    String rejectionReason
) { }
