package com.hr.leave.services;

import com.hr.leave.clients.EmployeeClient;
import com.hr.leave.clients.EmployeeClient.EmployeeInfo;
import com.hr.leave.dtos.request.CreateLeaveRequestDto;
import com.hr.leave.dtos.request.LeaveReportEntry;
import com.hr.leave.dtos.response.LeaveResponseDto;
import com.hr.leave.entities.LeaveRequest;
import com.hr.leave.enums.LeaveStatusEnum;
import com.hr.leave.repositories.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveService {

    private final LeaveRequestRepository repository;
    private final EmployeeClient employeeClient;

    @Transactional
    public LeaveResponseDto submit(CreateLeaveRequestDto req, UUID employeeId, String token) {
        if (req.endDate().isBefore(req.startDate()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must not be before startDate");

        EmployeeInfo employee = employeeClient.findById(employeeId, token);
        if (employee == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee not found");
        if (!employee.isActive())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee is not active");

        boolean overlaps = repository
                .findByEmployeeIdAndStatusIn(employeeId, Set.of(LeaveStatusEnum.PENDING, LeaveStatusEnum.APPROVED))
                .stream()
                .anyMatch(l -> !req.startDate().isAfter(l.getEndDate())
                        && !req.endDate().isBefore(l.getStartDate()));
        if (overlaps)
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Overlaps with an existing pending or approved leave request");

        LeaveRequest l = new LeaveRequest();
        l.setEmployeeId(employeeId);
        l.setLeaveType(req.leaveType());
        l.setStartDate(req.startDate());
        l.setEndDate(req.endDate());
        l.setReason(req.reason());
        l.setAttachmentUrl(req.attachmentUrl());
        return new LeaveResponseDto(
            l.getId(), l.getEmployeeId(), l.getLeaveType(),
            l.getStartDate(), l.getEndDate(), l.getTotalDays(),
            l.getReason(), l.getAttachmentUrl(), l.getStatus(),
            l.getReviewedBy(), l.getReviewedAt(), l.getRejectionReason()
        );
    }

    public List<LeaveResponseDto> findMine(UUID employeeId) {
        return repository.findByEmployeeIdOrderByCreatedAtDesc(employeeId).stream()
            .map(l ->
                new LeaveResponseDto(
                    l.getId(), l.getEmployeeId(), l.getLeaveType(),
                    l.getStartDate(), l.getEndDate(), l.getTotalDays(),
                    l.getReason(), l.getAttachmentUrl(), l.getStatus(),
                    l.getReviewedBy(), l.getReviewedAt(), l.getRejectionReason()
                )
            )
            .toList();
    }

    public List<LeaveResponseDto> findAll(LeaveStatusEnum status) {
        List<LeaveRequest> source = status == null ? repository.findAll() : repository.findByStatus(status);
        return source.stream()
                .sorted(Comparator.comparing(LeaveRequest::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(l ->
                    new LeaveResponseDto(
                        l.getId(), l.getEmployeeId(), l.getLeaveType(),
                        l.getStartDate(), l.getEndDate(), l.getTotalDays(),
                        l.getReason(), l.getAttachmentUrl(), l.getStatus(),
                        l.getReviewedBy(), l.getReviewedAt(), l.getRejectionReason()
                    )
                )
                .toList();
    }

    @Transactional
    public LeaveResponseDto approve(UUID id, UUID reviewerId) {
        LeaveRequest l = findPendingForReview(id, reviewerId);
        l.approve(reviewerId);
        return new LeaveResponseDto(
            l.getId(), l.getEmployeeId(), l.getLeaveType(),
            l.getStartDate(), l.getEndDate(), l.getTotalDays(),
            l.getReason(), l.getAttachmentUrl(), l.getStatus(),
            l.getReviewedBy(), l.getReviewedAt(), l.getRejectionReason()
        );
    }

    @Transactional
    public LeaveResponseDto reject(UUID id, UUID reviewerId, String reason) {
        LeaveRequest l = findPendingForReview(id, reviewerId);
        l.reject(reviewerId, reason);
        return new LeaveResponseDto(
            l.getId(), l.getEmployeeId(), l.getLeaveType(),
            l.getStartDate(), l.getEndDate(), l.getTotalDays(),
            l.getReason(), l.getAttachmentUrl(), l.getStatus(),
            l.getReviewedBy(), l.getReviewedAt(), l.getRejectionReason()
        );
    }

    public List<LeaveReportEntry> report() {
        return repository.findByStatus(LeaveStatusEnum.APPROVED).stream()
                .collect(Collectors.groupingBy(LeaveRequest::getEmployeeId))
                .entrySet().stream()
                .map(e -> new LeaveReportEntry(
                        e.getKey(),
                        e.getValue().stream().mapToLong(LeaveRequest::getTotalDays).sum(),
                        e.getValue().size()))
                .sorted(Comparator.comparingLong(LeaveReportEntry::totalDays).reversed())
                .toList();
    }

    private LeaveRequest findPendingForReview(UUID id, UUID reviewerId) {
        LeaveRequest l = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Leave request not found"));
        if (l.getEmployeeId().equals(reviewerId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot review your own leave request");
        if (l.getStatus() != LeaveStatusEnum.PENDING)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Leave request is already " + l.getStatus());
        return l;
    }
}