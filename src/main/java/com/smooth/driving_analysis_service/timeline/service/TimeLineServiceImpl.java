package com.smooth.driving_analysis_service.timeline.service;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.timeline.dto.DrivingRecordResponseDto;
import com.smooth.driving_analysis_service.timeline.dto.ReportSummaryResponseDto;
import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TimeLineServiceImpl implements TimeLineService {

    private final DrivingRecordRepository drivingRecordRepository;
    private final MilestoneReportRepository milestoneReportRepository;

    // ===== 주행 타임라인: 기존 구현 유지 =====
    @Override
    public TimeLineResponseDto getDrivingTimeLine(Long userId, String cursor, int limit) {
        final int pageSize = limit + 1;
        LocalDateTime before = parseCursor(cursor);
        PageRequest pr = PageRequest.of(0, pageSize);

        Page<DrivingRecord> page = (before != null)
                ? drivingRecordRepository.findByUserIdAndEndTimeBeforeOrderByEndTimeDesc(userId, before, pr)
                : drivingRecordRepository.findByUserIdOrderByEndTimeDesc(userId, pr);

        List<TimeLineResponseDto.TimeLineItem> items = page.getContent().stream()
                .map(this::toDrivingItemMinimal)
                .collect(Collectors.toList());

        boolean hasMore = items.size() > limit;
        if (hasMore) items = items.subList(0, limit);

        String nextCursor = items.isEmpty() ? null : toCursor(items.get(items.size() - 1).getCreatedAt());

        return TimeLineResponseDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    // ===== 리포트 타임라인: COLLECTING 제외, PROCESSING/COMPLETED만 노출 =====
    @Override
    public TimeLineResponseDto getReportTimeLine(Long userId, String cursor, int limit) {
        final int pageSize = limit + 1;
        LocalDateTime before = parseCursor(cursor);
        PageRequest pr = PageRequest.of(0, pageSize);

        List<MilestoneReport.Status> statuses = List.of(
                MilestoneReport.Status.PROCESSING,
                MilestoneReport.Status.COMPLETED
        );

        Page<MilestoneReport> page = (before != null)
                ? milestoneReportRepository.findByUserIdAndStatusInAndCreatedAtBeforeOrderByCreatedAtDesc(userId, statuses, before, pr)
                : milestoneReportRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(userId, statuses, pr);

        List<TimeLineResponseDto.TimeLineItem> items = page.getContent().stream()
                .map(this::toReportItem)
                .collect(Collectors.toList());

        boolean hasMore = items.size() > limit;
        if (hasMore) items = items.subList(0, limit);

        String nextCursor = items.isEmpty() ? null : toCursor(items.get(items.size() - 1).getCreatedAt());

        return TimeLineResponseDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    // ===== 전체(주행 + 리포트) =====
    @Override
    public TimeLineResponseDto getAllTimeLine(Long userId, String cursor, int limit) {
        int fetchSize = limit * 2 + 1; // 여유분
        LocalDateTime before = parseCursor(cursor);
        PageRequest pr = PageRequest.of(0, fetchSize);

        // 주행(기존 기준 그대로) – 다른 분 코드 시그니처에 맞춰 endTime 기반
        Page<DrivingRecord> dPage = (before != null)
                ? drivingRecordRepository.findByUserIdAndEndTimeBeforeOrderByEndTimeDesc(userId, before, pr)
                : drivingRecordRepository.findByUserIdOrderByEndTimeDesc(userId, pr);
        List<TimeLineResponseDto.TimeLineItem> drivingItems = dPage.getContent().stream()
                .map(this::toDrivingItemMinimal)
                .collect(Collectors.toList());

        // 리포트 – COLLECTING 제외
        List<MilestoneReport.Status> statuses = List.of(
                MilestoneReport.Status.PROCESSING,
                MilestoneReport.Status.COMPLETED
        );
        Page<MilestoneReport> rPage = (before != null)
                ? milestoneReportRepository.findByUserIdAndStatusInAndCreatedAtBeforeOrderByCreatedAtDesc(userId, statuses, before, pr)
                : milestoneReportRepository.findByUserIdAndStatusInOrderByCreatedAtDesc(userId, statuses, pr);
        List<TimeLineResponseDto.TimeLineItem> reportItems = rPage.getContent().stream()
                .map(this::toReportItem)
                .collect(Collectors.toList());

        // 머지 후 createdAt DESC
        List<TimeLineResponseDto.TimeLineItem> merged = new ArrayList<>(drivingItems.size() + reportItems.size());
        merged.addAll(drivingItems);
        merged.addAll(reportItems);
        merged.sort(Comparator.comparing(TimeLineResponseDto.TimeLineItem::getCreatedAt).reversed());

        boolean hasMore = merged.size() > limit;
        if (hasMore) merged = merged.subList(0, limit);

        String nextCursor = merged.isEmpty() ? null : toCursor(merged.get(merged.size() - 1).getCreatedAt());

        return TimeLineResponseDto.builder()
                .items(merged)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    // ====== private helpers ======

    private LocalDateTime parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) return null;
        try {
            return LocalDateTime.parse(cursor); // 'yyyy-MM-ddTHH:mm:ss[.SSS]' 허용
        } catch (Exception e) {
            try {
                return OffsetDateTime.parse(cursor).toLocalDateTime();
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private String toCursor(LocalDateTime dt) {
        return dt == null ? null : dt.toString();
    }

    // 프론트 스펙에 맞춘 최소 주행 아이템 매핑 (Driving 쪽 로직은 변경하지 않음)
    private TimeLineResponseDto.TimeLineItem toDrivingItemMinimal(DrivingRecord dr) {
        // createdAt: 시작시간 우선, 없으면 종료시간
        LocalDateTime created = dr.getStartTime() != null ? dr.getStartTime() : dr.getEndTime();

        Integer minutes = null;
        if (dr.getStartTime() != null && dr.getEndTime() != null) {
            minutes = (int) ChronoUnit.MINUTES.between(dr.getStartTime(), dr.getEndTime());
        }

        // 평균/거리/정속률 타입 일치 (null 안전)
        Double totalKm = dr.getTotalDistance() == null ? null : dr.getTotalDistance().doubleValue();
        Double avgSpeed = dr.getAvgSpeed() == null ? null : dr.getAvgSpeed().doubleValue();
        Double cruiseRatio = null;
        if (dr.getCruiseRatio() != null) {
            double v = dr.getCruiseRatio();
            // 저장이 0.0~1.0 비율일 경우 → % 로 변환
            cruiseRatio = (v <= 1.0) ? v * 100.0 : v;
        }

        return TimeLineResponseDto.TimeLineItem.builder()
                .id("drive_" + dr.getId())     // 프론트 스펙: drive_{id}
                .type("DRIVING")
                .createdAt(created)
                .data(DrivingRecordResponseDto.builder()
                        .id(dr.getId())
                        .startTime(dr.getStartTime())
                        .endTime(dr.getEndTime())
                        .totalDistance(totalKm)
                        .avgSpeed(avgSpeed)
                        .cruiseRatio(cruiseRatio)
                        .laneChangeCount(dr.getLaneChangeCount())
                        .hardBrakeCount(dr.getHardBrakeCount())
                        .rapidAccelCount(dr.getRapidAccelCount())
                        .sharpTurnCount(dr.getSharpTurnCount())
                        .drivingMinutes(minutes)
                        .status("COMPLETED")
                        .build())
                .build();
    }

    private TimeLineResponseDto.TimeLineItem toReportItem(MilestoneReport mr) {
        // 상태는 엔티티 그대로 문자열화: COLLECTING / PROCESSING / COMPLETED
        String status = mr.getStatus().name();

        return TimeLineResponseDto.TimeLineItem.builder()
                .id("report_" + mr.getId())         // 프론트 스펙: report_{id}
                .type("REPORT")
                .createdAt(mr.getCreatedAt())
                .data(ReportSummaryResponseDto.builder()
                        .id(mr.getId())
                        .isRead(mr.isRead())
                        .status(status)
                        .build())
                .build();
    }
}
