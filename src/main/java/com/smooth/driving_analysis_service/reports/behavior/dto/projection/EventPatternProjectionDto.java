package com.smooth.driving_analysis_service.reports.behavior.dto.projection;

/**
 * Athena 쿼리 결과를 매핑하기 위한 Projection 인터페이스
 * S3 event_data에서 요일별, 시간대별, 행동별 집계 데이터
 */
public interface EventPatternProjectionDto {
    Integer getWeekday();        // 1=월요일, 2=화요일, ..., 7=일요일
    String getTimeSlot();        // DAWN, COMMUTE_TO_WORK, DAYTIME, COMMUTE_FROM_WORK, EVENING
    String getEventType();       // rapid_accel, hard_brake, lane_change
    Integer getEventCount();     // 해당 조건의 이벤트 발생 횟수
}