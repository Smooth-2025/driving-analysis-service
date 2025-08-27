package com.smooth.driving_analysis_service.reports.milestone.service;

import com.smooth.driving_analysis_service.global.exception.BusinessException;
import com.smooth.driving_analysis_service.global.exception.CommonErrorCode;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MilestoneReadService {

    private final MilestoneReportRepository reportRepository;

    @Transactional
    public ReadResult toggle(Long reportId) {
        MilestoneReport report = find(reportId);
        boolean prev = Boolean.TRUE.equals(report.getIsRead());
        report.setRead(!prev);
        return new ReadResult(report.getId(), prev, report.getIsRead(), "toggle");
    }

    @Transactional
    public ReadResult set(Long reportId, boolean read) {
        MilestoneReport report = find(reportId);
        boolean prev = Boolean.TRUE.equals(report.getIsRead());
        report.setRead(read);
        return new ReadResult(report.getId(), prev, report.getIsRead(), "set");
    }

    private MilestoneReport find(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "report not found: " + id));
    }

    public record ReadResult(Long reportId, boolean previous, boolean current, String mode) {}
}
