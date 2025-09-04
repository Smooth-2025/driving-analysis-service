package com.smooth.driving_analysis_service.trigger.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder
public class ReportTriggerV1 {
    private int v;
    private String type; // INTERIM, FINAL
    private String userId;
    private Long reportId;
    private int milestone;
    private List<String> drivingIds; // 내부 DTO는 drivingIds로 유지
    private String status;
    private LocalDateTime emittedAt;
    private String producer;
    private String traceId;
}