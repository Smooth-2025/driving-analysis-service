package com.smooth.driving_analysis_service.trigger.producer;

import com.smooth.driving_analysis_service.trigger.dto.ReportTriggerV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportTriggerProducer {

    private final RedisTemplate<String, String> redis;
    // 환경파일에서 REPORT_STREAM_NAME=report.trigger 로 고정
    private static final String STREAM = "report.trigger";

    public RecordId emit(ReportTriggerV1 t) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("v", String.valueOf(t.getV()));
        fields.put("userId", t.getUserId());
        fields.put("reportId", String.valueOf(t.getReportId()));
        fields.put("milestone", String.valueOf(t.getMilestone()));
        fields.put("tripIds", String.join(",", t.getDrivingIds())); // 필드명은 tripIds로 내보내는 정책 유지
        fields.put("status", t.getStatus());
        fields.put("emittedAt", t.getEmittedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS")));
        fields.put("producer", t.getProducer());
        fields.put("traceId", t.getTraceId());

        RecordId id = redis.opsForStream().add(STREAM, fields);
        log.info("report.trigger XADD id={}, reportId={}, userId={}", id.getValue(), t.getReportId(), t.getUserId());
        return id;
    }
}
