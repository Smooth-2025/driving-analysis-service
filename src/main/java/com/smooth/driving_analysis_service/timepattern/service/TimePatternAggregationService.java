// src/main/java/com/smooth/driving_analysis_service/timepattern/service/TimePatternAggregationService.java
package com.smooth.driving_analysis_service.timepattern.service;

import com.smooth.driving_analysis_service.timepattern.dto.TimePatternGridCellDto;
import com.smooth.driving_analysis_service.timepattern.repository.TimePatternJdbcRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TimePatternAggregationService {

    private final TimePatternJdbcRepository repo;

    @Value("${timepattern.useConvertTz:true}")
    private boolean useConvertTz;

    public List<TimePatternGridCellDto> getGridForLastNTrips(String userId, Integer limitN) {
        int n = (limitN == null || limitN <= 0) ? 15 : limitN;
        return repo.findTimeBinsForLastNTrips(userId, n, useConvertTz);
    }
}
