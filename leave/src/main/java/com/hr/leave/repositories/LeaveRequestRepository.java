package com.hr.leave.repositories;

import com.hr.leave.entities.LeaveRequest;
import com.hr.leave.enums.LeaveStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {

    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(UUID employeeId);

    List<LeaveRequest> findByStatus(LeaveStatusEnum status);

    List<LeaveRequest> findByEmployeeIdAndStatusIn(UUID employeeId, Collection<LeaveStatusEnum> statuses);
}