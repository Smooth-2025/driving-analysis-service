package com.smooth.driving_analysis_service.trigger.producer;

import com.smooth.driving_analysis_service.batch.dto.ReportTriggerV1;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;


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
        fields.put("type", t.getType());
        fields.put("userId", t.getUserId());
        fields.put("reportId", t.getReportId());
        fields.put("milestone", t.getMilestone());
        fields.put("drivingIds", String.join(",", t.getDrivingIds()));
        fields.put("status", t.getStatus());

        RecordId id = redis.opsForStream().add(STREAM, fields);
        log.info("report.trigger XADD id={}, reportId={}, userId={}, type={}", 
                id.getValue(), t.getReportId(), t.getUserId(), t.getType());
        return id;
    }
}