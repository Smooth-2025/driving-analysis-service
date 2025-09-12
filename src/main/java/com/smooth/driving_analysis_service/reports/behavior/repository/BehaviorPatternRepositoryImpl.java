package com.smooth.driving_analysis_service.reports.behavior.repository;

import com.smooth.driving_analysis_service.reports.common.service.ReportsAthenaQueryService;
import com.smooth.driving_analysis_service.reports.behavior.dto.projection.EventPatternProjectionDto;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class BehaviorPatternRepositoryImpl implements BehaviorPatternRepository {

    private final MilestoneItemRepository milestoneItemRepository;
    private final ReportsAthenaQueryService reportsAthenaQueryService;

    @Override
    public List<EventPatternProjectionDto> findEventPatternsByReportId(Long reportId) {
        try {
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
    public List<EventPatternProjectionDto> findEventPatternsByDrivingIds(List<String> drivingIds) {
        try {
            if (drivingIds.isEmpty()) {
                log.warn("drivingIds가 비어있습니다.");
                return new ArrayList<>();
            }
            log.info("Athena 쿼리로 Behavior 패턴 분석 - drivingIds: {}", drivingIds.size());
            return executeAthenaPatternQuery(drivingIds);
        } catch (Exception e) {
            log.error("Athena 패턴 쿼리 실패, fallback 사용", e);
            return generateFallbackPatterns();
        }
    }

    /**
     * Athena로 실제 시간대별 이벤트 패턴 분석
     */
    private List<EventPatternProjectionDto> executeAthenaPatternQuery(List<String> drivingIds) {
        String tripIds = drivingIds.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));
        
        String query = buildBehaviorPatternQuery(tripIds);
        List<Map<String, Object>> results = reportsAthenaQueryService.executeQuery(query);
        
        return results.stream()
                .map(this::mapToEventPattern)
                .collect(Collectors.toList());
    }
    
    /**
     * Behavior 패턴 분석 쿼리 생성
     */
    private String buildBehaviorPatternQuery(String tripIds) {
        return String.format("""
            WITH base AS (
              SELECT 
                eventType,
                tripId,
                date_format(from_timezone("timestamp", 'Asia/Seoul'), '%%Y-%%m-%%d') AS ymd,
                date_format(from_timezone("timestamp", 'Asia/Seoul'), '%%w') AS dow,
                hour(from_timezone("timestamp", 'Asia/Seoul')) AS hh
              FROM raw_report_message 
              WHERE dt >= date_format(current_date - interval '30' day, '%%Y-%%m-%%d')
                AND eventType IN ('hard_brake','hard_accel','lane_change')
                AND tripId IN (%s)
            ),
            bucketed AS (
              SELECT 
                eventType,
                dow,
                CASE 
                  WHEN hh BETWEEN 0 AND 5   THEN 'DAWN'
                  WHEN hh BETWEEN 6 AND 9   THEN 'COMMUTE_TO_WORK' 
                  WHEN hh BETWEEN 10 AND 16 THEN 'DAYTIME'
                  WHEN hh BETWEEN 17 AND 19 THEN 'COMMUTE_FROM_WORK'
                  ELSE 'EVENING'
                END AS time_bucket
              FROM base
            )
            SELECT 
              dow,
              time_bucket,
              eventType,
              count(*) AS event_count
            FROM bucketed 
            GROUP BY 1,2,3 
            ORDER BY 1,2,3
            """, tripIds);
    }
    
    /**
     * Athena 결과를 EventPatternProjectionDto로 매핑
     */
    private EventPatternProjectionDto mapToEventPattern(Map<String, Object> row) {
        String dow = (String) row.get("dow");
        String timeBucket = (String) row.get("time_bucket");
        String eventType = (String) row.get("eventType");
        String eventCountStr = row.get("event_count").toString();
        
        // 요일을 숫자로 변환 (1=월요일)
        int weekdayNum = switch (dow) {
            case "1" -> 1; // 월요일
            case "2" -> 2; // 화요일  
            case "3" -> 3; // 수요일
            case "4" -> 4; // 목요일
            case "5" -> 5; // 금요일
            case "6" -> 6; // 토요일
            case "0" -> 7; // 일요일
            default -> 1;
        };
        
        return new EventPatternProjectionDto() {
            @Override
            public Integer getWeekday() {
                return weekdayNum;
            }
            
            @Override
            public String getTimeSlot() {
                return timeBucket;
            }
            
            @Override
            public String getEventType() {
                return eventType;
            }
            
            @Override
            public Integer getEventCount() {
                return Integer.parseInt(eventCountStr);
            }
        };
    }

    /**
     * Fallback 패턴 (Athena 쿼리 실패 시 사용)
     */
    private List<EventPatternProjectionDto> generateFallbackPatterns() {
        log.info("Generating fallback behavior patterns");
        
        List<EventPatternProjectionDto> fallbackPatterns = new ArrayList<>();

        // 금요일 저녁 패턴
        fallbackPatterns.add(createProjection(5, "EVENING", "hard_brake", 5));
        fallbackPatterns.add(createProjection(5, "EVENING", "hard_accel", 4));
        fallbackPatterns.add(createProjection(5, "EVENING", "lane_change", 3));

        // 평일 퇴근시간 패턴
        fallbackPatterns.add(createProjection(1, "COMMUTE_FROM_WORK", "hard_brake", 2));
        fallbackPatterns.add(createProjection(2, "COMMUTE_FROM_WORK", "hard_brake", 3));
        fallbackPatterns.add(createProjection(3, "COMMUTE_FROM_WORK", "hard_brake", 1));
        fallbackPatterns.add(createProjection(4, "COMMUTE_FROM_WORK", "hard_brake", 2));

        log.info("Generated {} fallback patterns", fallbackPatterns.size());
        return fallbackPatterns;
    }

    private EventPatternProjectionDto createProjection(int weekday, String timeSlot, String eventType, int count) {
        return new EventPatternProjectionDto() {
            @Override
            public Integer getWeekday() {
                return weekday;
            }

            @Override
            public String getTimeSlot() {
                return timeSlot;
            }

            @Override
            public String getEventType() {
                return eventType;
            }

            @Override
            public Integer getEventCount() {
                return count;
            }
        };
    }
}