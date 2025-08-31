package com.smooth.driving_analysis_service.timeline.service;

import com.smooth.driving_analysis_service.timeline.dto.DrivingRecordResponseDto;
import com.smooth.driving_analysis_service.driving.entity.DrivingRecord;
import com.smooth.driving_analysis_service.driving.repository.DrivingRecordRepository;
import com.smooth.driving_analysis_service.timeline.dto.TimeLineResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class TimeLineServiceImpl implements TimeLineService {

    private static final String DRIVING_ID_PREFIX = "drive_";
    private static final String REPORT_ID_PREFIX = "report_";
    
    private final DrivingRecordRepository drivingRecordRepository;

    @Override
    public TimeLineResponseDto getDrivingTimeLine(Long userId, String cursor, int limit) {
        LocalDateTime cursorTime = cursor != null ? LocalDateTime.parse(cursor) : LocalDateTime.now();

        List<DrivingRecord> drivingRecords;

        if (cursor != null) {
            drivingRecords = drivingRecordRepository.findByUserIdAndCreatedAtBefore(
                    userId, cursorTime, PageRequest.of(0, limit + 1)  // +1로 hasMore 판단
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

        List<TimeLineResponseDto.TimeLineItem> timeLineItems = drivingRecords.stream()
                .map(record -> TimeLineResponseDto.TimeLineItem.builder()
                        .id(DRIVING_ID_PREFIX + record.getId())
                        .type("DRIVING")
                        .createdAt(record.getCreatedAt())
                        .data(DrivingRecordResponseDto.from(record))
                        .build())
                .collect(Collectors.toList());

        String nextCursor = timeLineItems.isEmpty() ? null :
                timeLineItems.get(timeLineItems.size() - 1).getCreatedAt().toString();

        return TimeLineResponseDto.builder()
                .items(timeLineItems)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

}
