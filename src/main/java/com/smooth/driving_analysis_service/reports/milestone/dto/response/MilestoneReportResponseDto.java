package com.smooth.driving_analysis_service.reports.milestone.dto.response;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MilestoneReportResponseDto {
    private Long id;
    private String reportId;
    private Long userId;
    private Integer cycleNo;
    private Integer numberOfDriving;
    private String status;      // COLLECTING / PROCESSING / COMPLETED
    private boolean read;
    private LocalDateTime createdAt;

    public static MilestoneReportResponseDto from(MilestoneReport m) {
        return MilestoneReportResponseDto.builder()
                .id(m.getId())
                .reportId(m.getReportId())
                .userId(m.getUserId())
                .cycleNo(m.getCycleNo())
                .numberOfDriving(m.getNumberOfDriving())
                .status(m.getStatus().name())
                .read(m.isRead())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
