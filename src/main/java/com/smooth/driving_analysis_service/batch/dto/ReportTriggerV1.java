package com.smooth.driving_analysis_service.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTriggerV1 {
    
    private String type; // "INTERIM" or "FINAL"
    private String userId;
    private String reportId;
    private String milestone;
    private String status;
    private List<String> drivingIds;
    private int v; // version
}