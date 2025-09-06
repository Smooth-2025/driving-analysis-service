package com.smooth.driving_analysis_service.reports.behavior.repository;

import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjection;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BehaviorPatternRepositoryImpl implements BehaviorPatternRepository {

    private final MilestoneItemRepository milestoneItemRepository;

    @Override
    public List<EventPatternProjection> findEventPatternsByReportId(Long reportId) {
        try {
            // 1. 해당 리포트의 drivingId 목록 조회
            List<String> drivingIds = milestoneItemRepository.findDrivingIdsByReportId(reportId);
            
            if (drivingIds.isEmpty()) {
                log.warn("리포트에 해당하는 drivingId가 없습니다. reportId: {}", reportId);
                return new ArrayList<>();
            }

            return findEventPatternsByDrivingIds(drivingIds);

        } catch (Exception e) {
            log.error("이벤트 패턴 조회 실패 - reportId: {}", reportId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<EventPatternProjection> findEventPatternsByDrivingIds(List<String> drivingIds) {
        try {
            if (drivingIds.isEmpty()) {
                log.warn("drivingIds가 비어있습니다.");
                return new ArrayList<>();
            }

            // TODO: 실제 Athena 쿼리 구현 예정 - 현재는 Mock 데이터 사용
            log.info("Mock 데이터 사용 - drivingIds: {}", drivingIds);
            return generateMockEventPatterns(drivingIds);

        } catch (Exception e) {
            log.error("이벤트 패턴 조회 실패 - drivingIds: {}", drivingIds, e);
            return new ArrayList<>();
        }
    }

    /**
     * TODO: 실제 Athena 쿼리로 대체 예정
     * 현재는 테스트를 위한 Mock 데이터 생성
     */
    private List<EventPatternProjection> generateMockEventPatterns(List<String> drivingIds) {
        List<EventPatternProjection> mockPatterns = new ArrayList<>();
        
        // 금요일 저녁에 가장 많은 위험행동이 발생하는 패턴 생성
        mockPatterns.add(createMockProjection(5, "EVENING", "hard_brake", 8));
        mockPatterns.add(createMockProjection(5, "EVENING", "rapid_accel", 6));
        mockPatterns.add(createMockProjection(5, "EVENING", "lane_change", 7));
        
        // 평일 퇴근시간 패턴
        mockPatterns.add(createMockProjection(1, "COMMUTE_FROM_WORK", "hard_brake", 3));
        mockPatterns.add(createMockProjection(2, "COMMUTE_FROM_WORK", "hard_brake", 4));
        mockPatterns.add(createMockProjection(3, "COMMUTE_FROM_WORK", "hard_brake", 2));
        mockPatterns.add(createMockProjection(4, "COMMUTE_FROM_WORK", "hard_brake", 5));
        
        // 출근시간 급가속 패턴
        mockPatterns.add(createMockProjection(1, "COMMUTE_TO_WORK", "rapid_accel", 2));
        mockPatterns.add(createMockProjection(2, "COMMUTE_TO_WORK", "rapid_accel", 3));
        mockPatterns.add(createMockProjection(3, "COMMUTE_TO_WORK", "rapid_accel", 1));
        mockPatterns.add(createMockProjection(4, "COMMUTE_TO_WORK", "rapid_accel", 4));
        
        // 낮시간 차선변경 패턴
        mockPatterns.add(createMockProjection(1, "DAYTIME", "lane_change", 2));
        mockPatterns.add(createMockProjection(2, "DAYTIME", "lane_change", 3));
        mockPatterns.add(createMockProjection(3, "DAYTIME", "lane_change", 1));
        mockPatterns.add(createMockProjection(4, "DAYTIME", "lane_change", 2));
        mockPatterns.add(createMockProjection(5, "DAYTIME", "lane_change", 4));
        
        return mockPatterns;
    }

    private EventPatternProjection createMockProjection(int weekday, String timeSlot, String eventType, int count) {
        return new EventPatternProjection() {
            @Override
            public Integer getWeekday() { return weekday; }
            
            @Override
            public String getTimeSlot() { return timeSlot; }
            
            @Override
            public String getEventType() { return eventType; }
            
            @Override
            public Integer getEventCount() { return count; }
        };
    }

    // TODO: 실제 Athena 쿼리 구현시 사용할 메서드들
    /*
    private String buildEventPatternQuery(List<String> drivingIds) {
        String drivingIdList = drivingIds.stream()
                .map(id -> "'" + id + "'")
                .reduce((a, b) -> a + "," + b)
                .orElse("''");

        return String.format("""
            SELECT 
              EXTRACT(DOW FROM CAST(timestamp AS timestamp)) as weekday,
              CASE 
                WHEN EXTRACT(HOUR FROM CAST(timestamp AS timestamp)) BETWEEN 0 AND 5 THEN 'DAWN'
                WHEN EXTRACT(HOUR FROM CAST(timestamp AS timestamp)) BETWEEN 6 AND 9 THEN 'COMMUTE_TO_WORK'
                WHEN EXTRACT(HOUR FROM CAST(timestamp AS timestamp)) BETWEEN 10 AND 16 THEN 'DAYTIME'
                WHEN EXTRACT(HOUR FROM CAST(timestamp AS timestamp)) BETWEEN 17 AND 19 THEN 'COMMUTE_FROM_WORK'
                ELSE 'EVENING'
              END as time_slot,
              eventType as event_type,
              COUNT(*) as event_count
            FROM event_data 
            WHERE drivingId IN (%s)
              AND eventType IN ('rapid_accel', 'hard_brake', 'lane_change')
            GROUP BY 1, 2, 3
            ORDER BY weekday, time_slot, event_type
            """, drivingIdList);
    }

    private EventPatternProjection mapToProjection(Map<String, Object> row) {
        return new EventPatternProjection() {
            @Override
            public Integer getWeekday() {
                return ((Number) row.get("weekday")).intValue();
            }

            @Override
            public String getTimeSlot() {
                return (String) row.get("time_slot");
            }

            @Override
            public String getEventType() {
                return (String) row.get("event_type");
            }

            @Override
            public Integer getEventCount() {
                return ((Number) row.get("event_count")).intValue();
            }
        };
    }
    */
}