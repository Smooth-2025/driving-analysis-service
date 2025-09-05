package com.smooth.driving_analysis_service.batch.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTriggerV1 {
    
    private int v; // 버전
    private String type; // "INTERIM" or "FINAL"
    private String userId;
    private Long reportId;
    private Integer milestone; // 4, 8, 12, 15
    private String status; // "COLLECTING", "PROCESSING", "COMPLETED"
    private List<String> drivingIds;
    private LocalDateTime emittedAt; // 트리거 발행 시간
    private String producer; // 발행자
    private String traceId; // 추적 ID
    
    public boolean isInterim() {
        return "INTERIM".equals(type);
    }
    
    public boolean isFinal() {
        return "FINAL".equals(type);
    }
}