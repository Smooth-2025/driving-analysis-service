// reports/accident_reaction/service/AccidentResponseService.java
package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import java.util.Map;

public interface AccidentResponseService {
    Map<String, Object> buildAccidentResponse(Long reportId);
}
