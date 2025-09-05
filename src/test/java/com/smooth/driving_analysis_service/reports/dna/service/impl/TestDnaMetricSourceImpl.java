package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.reports.dna.service.DnaMetricSource;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@Profile("test")
public class TestDnaMetricSourceImpl implements DnaMetricSource {

    @Override
    public DnaInput loadForReport(Long reportId, List<String> drivingIds) {
        // 테스트용 목업 데이터 생성
        List<PerDriving> drivings = IntStream.range(0, drivingIds.size())
                .mapToObj(i -> createMockPerDriving(drivingIds.get(i), i))
                .collect(Collectors.toList());
        
        return new DnaInput(drivings);
    }

    private PerDriving createMockPerDriving(String drivingId, int index) {
        // 다양한 패턴의 테스트 데이터 생성
        double baseValue = (index % 3) + 1; // 1, 2, 3 순환
        
        return new PerDriving(
                drivingId,
                5.0 + baseValue * 2, // sec0to40: 7, 9, 11초
                1.0 + baseValue * 0.3, // avgDecelRate: 1.3, 1.6, 1.9
                0.5 + baseValue * 0.5, // laneChangePerKm: 1.0, 1.5, 2.0
                0.1 + baseValue * 0.1, // postChangeAccel: 0.2, 0.3, 0.4
                10.0 + baseValue * 5 // distanceKm: 15, 20, 25km
        );
    }
}