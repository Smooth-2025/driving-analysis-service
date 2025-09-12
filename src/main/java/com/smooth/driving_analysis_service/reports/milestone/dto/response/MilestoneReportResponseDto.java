package com.smooth.driving_analysis_service.reports.milestone.dto.response;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MilestoneReportResponseDto {
    private Long id;
    private String reportId;
    private Integer numberOfDriving;
    private String status;
    private Boolean read;

    public static MilestoneReportResponseDto from(MilestoneReport report) {
        return MilestoneReportResponseDto.builder()
                .id(report.getId())
                .reportId(report.getReportId())
                .numberOfDriving(report.getNumberOfDriving())
                .status(report.getStatus().name())
                .read(report.getRead())
                .build();
    }
}