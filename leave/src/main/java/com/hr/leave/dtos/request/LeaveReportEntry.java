package com.hr.leave.dtos.request;

import java.util.UUID;

public record LeaveReportEntry(
    UUID employeeId,
    long totalDays,
    long requestCount
) {}