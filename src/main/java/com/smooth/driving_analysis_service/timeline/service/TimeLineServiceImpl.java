package com.smooth.driving_analysis_service.timeline.service;

import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.entity.SummaryStatus;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import com.smooth.driving_analysis_service.reports.milestone.repository.MilestoneReportRepository;
import com.smooth.driving_analysis_service.timeline.dto.DrivingRecordResponseDto;
import com.smooth.driving_analysis_service.timeline.dto.ReportSummaryResponseDto;
import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class TimeLineServiceImpl implements TimeLineService {

    private final DrivingRecordRepository drivingRecordRepository;
    private final MilestoneReportRepository milestoneReportRepository;

    @Override
    public TimeLineResponseDto getDrivingTimeLine(Long userId, String cursor, int limit) {
        LocalDateTime cursorTime = cursor != null ? LocalDateTime.parse(cursor) : LocalDateTime.now();

        List<DrivingRecord> drivingRecords;

        if (cursor != null) {
            drivingRecords = drivingRecordRepository.findByUserIdAndCreatedAtBefore(
                    userId, cursorTime, PageRequest.of(0, limit + 1)
            );
        } else {
            drivingRecords = drivingRecordRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, PageRequest.of(0, limit + 1)
            );
        }

        boolean hasMore = drivingRecords.size() > limit;
        if (hasMore) {
            drivingRecords = drivingRecords.subList(0, limit);
        }

        List<TimeLineResponseDto.TimeLineItem> items = drivingRecords.stream()
                .map(this::toDrivingItem)
                .toList();

        String nextCursor = items.isEmpty() ? null :
                items.get(items.size() - 1).getCreatedAt().toString();

        return TimeLineResponseDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }


    @Override
    public TimeLineResponseDto getReportTimeLine(Long userId, String cursor, int limit) {
        final int pageSize = limit + 1;
        LocalDateTime before = parseCursor(cursor);
        PageRequest pr = PageRequest.of(0, pageSize);

        List<MilestoneReport.Status> statuses = List.of(
                MilestoneReport.Status.PROCESSING,
                MilestoneReport.Status.COMPLETED);

        Page<MilestoneReport> page = (before != null)
                ? milestoneReportRepository.findByUserIdAndStatusInAndUpdatedAtBeforeOrderByUpdatedAtDesc(userId,
                        statuses, before, pr)
                : milestoneReportRepository.findByUserIdAndStatusInOrderByUpdatedAtDesc(userId, statuses, pr);

        List<TimeLineResponseDto.TimeLineItem> items = page.getContent().stream()
                .map(this::toReportItem)
                .toList();

        boolean hasMore = items.size() > limit;
        if (hasMore)
            items = items.subList(0, limit);

        String nextCursor = items.isEmpty() ? null : toCursor(items.get(items.size() - 1).getCreatedAt());

        return TimeLineResponseDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }


    @Override
    public TimeLineResponseDto getAllTimeLine(Long userId, String cursor, int limit) {
        log.info("전체 타임라인 조회 시작 - userId: {}, cursor: {}, limit: {}", userId, cursor, limit);

        try {
            int fetchSize = limit * 2 + 1; // 여유분
            LocalDateTime before = parseCursor(cursor);
            PageRequest pr = PageRequest.of(0, fetchSize);

            List<DrivingRecord> drivingRecords = (before != null)
                    ? drivingRecordRepository.findByUserIdAndCreatedAtBefore(userId, before, pr)
                    : drivingRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, pr);
            List<TimeLineResponseDto.TimeLineItem> drivingItems = drivingRecords.stream()
                    .map(this::toDrivingItem)
                    .toList();
            log.info("주행 아이템 개수: {}", drivingItems.size());

            // 리포트 – COLLECTING 제외
            List<MilestoneReport.Status> statuses = List.of(
                    MilestoneReport.Status.PROCESSING,
                    MilestoneReport.Status.COMPLETED);
            Page<MilestoneReport> rPage = (before != null)
                    ? milestoneReportRepository.findByUserIdAndStatusInAndUpdatedAtBeforeOrderByUpdatedAtDesc(userId,
                            statuses, before, pr)
                    : milestoneReportRepository.findByUserIdAndStatusInOrderByUpdatedAtDesc(userId, statuses, pr);
            List<TimeLineResponseDto.TimeLineItem> reportItems = rPage.getContent().stream()
                    .map(this::toReportItem)
                    .toList();
            log.info("리포트 아이템 개수: {}", reportItems.size());

            // null createdAt 체크
            long nullDrivingCount = drivingItems.stream().filter(item -> item.getCreatedAt() == null).count();
            long nullReportCount = reportItems.stream().filter(item -> item.getCreatedAt() == null).count();
            if (nullDrivingCount > 0 || nullReportCount > 0) {
                log.warn("null createdAt 발견 - 주행: {}, 리포트: {}", nullDrivingCount, nullReportCount);
            }

            // 머지 후 createdAt DESC (null 안전 처리)
            List<TimeLineResponseDto.TimeLineItem> merged = new ArrayList<>(drivingItems.size() + reportItems.size());
            merged.addAll(drivingItems);
            merged.addAll(reportItems);
            merged.sort(Comparator.comparing(TimeLineResponseDto.TimeLineItem::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())).reversed());

            boolean hasMore = merged.size() > limit;
            if (hasMore)
                merged = merged.subList(0, limit);

            String nextCursor = merged.isEmpty() ? null : toCursor(merged.get(merged.size() - 1).getCreatedAt());

            log.info("전체 타임라인 조회 완료 - 최종 아이템 개수: {}, hasMore: {}", merged.size(), hasMore);
            return TimeLineResponseDto.builder()
                    .items(merged)
                    .nextCursor(nextCursor)
                    .hasMore(hasMore)
                    .build();
        } catch (Exception e) {
            log.error("전체 타임라인 조회 중 오류 발생", e);
            throw e;
        }
    }

    // ====== private helpers ======

    private LocalDateTime parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank())
            return null;
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

    private TimeLineResponseDto.TimeLineItem toDrivingItem(DrivingRecord dr) {

        return TimeLineResponseDto.TimeLineItem.builder()
                .id("drive_" + dr.getId())
                .type("DRIVING")
                .createdAt(dr.getCreatedAt())
                .status(dr.getStatus().toString())
                .data(dr.getStatus() == SummaryStatus.PROCESSING ?
                        null : DrivingRecordResponseDto.from(dr))
                .build();
    }

    private TimeLineResponseDto.TimeLineItem toReportItem(MilestoneReport mr) {

        LocalDateTime created = mr.getUpdatedAt() != null ? mr.getUpdatedAt()
                : mr.getCreatedAt() != null ? mr.getCreatedAt() : LocalDateTime.now();

        if (mr.getUpdatedAt() == null && mr.getCreatedAt() == null) {
            log.warn("마일스톤 리포트 ID {}에 updatedAt과 createdAt이 모두 null입니다", mr.getId());
        }

        return TimeLineResponseDto.TimeLineItem.builder()
                .id("report_" + mr.getId())
                .type("REPORT")
                .createdAt(created)
                .status(mr.getStatus().name())
                .data(ReportSummaryResponseDto.builder()
                        .id(mr.getId())
                        .isRead(mr.isRead())
                        .build())
                .build();
    }
}
