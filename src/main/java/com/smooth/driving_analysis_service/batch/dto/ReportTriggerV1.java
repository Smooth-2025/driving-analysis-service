package com.smooth.driving_analysis_service.batch.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTriggerV1 {
    
    private String type; // "INTERIM" or "FINAL"
    private String userId;
    private Long reportId;
    private Integer milestone; // 4, 8, 12, 15
    private String status; // "COLLECTING", "PROCESSING", "COMPLETED"
    private List<String> drivingIds;
    
    public boolean isInterim() {
        return "INTERIM".equals(type);
    }
    
    public boolean isFinal() {
        return "FINAL".equals(type);
    }
}