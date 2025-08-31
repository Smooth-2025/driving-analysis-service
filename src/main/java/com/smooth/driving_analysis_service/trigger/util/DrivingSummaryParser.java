package com.smooth.driving_analysis_service.trigger.util;

import com.smooth.driving_analysis_service.trigger.dto.DrivingSummaryV1;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DrivingSummaryParser {

    public DrivingSummaryV1 parse(Map<String, String> f) {
        DrivingSummaryV1 s = new DrivingSummaryV1();
        s.setV(parseInt(f.getOrDefault("v", "1")));
        s.setUserId(f.get("userId"));
        s.setDrivingId(f.get("drivingId"));
        s.setStartedAt(parseLongOrNull(f.get("startedAt")));
        s.setEndedAt(parseLong(f.get("endedAt")));
        s.setStatus(f.get("status"));
        s.setProducer(f.get("producer"));

        s.setDurationS(parseIntOrNull(f.get("duration_s")));
        s.setDistanceM(parseIntOrNull(f.get("distance_m")));
        s.setEvHardBrake(parseIntOrNull(f.get("ev_hard_brake")));
        s.setEvRapidAccel(parseIntOrNull(f.get("ev_rapid_accel")));
        s.setEvLaneChange(parseIntOrNull(f.get("ev_lane_change")));
        // moving_s, s3Key는 사용 안함 → 파싱 제외

        s.validateForProcessing();
        return s;
    }

    private int parseInt(String v) { return Integer.parseInt(v); }
    private Long parseLongOrNull(String v) { return v == null ? null : Long.parseLong(v); }
    private long parseLong(String v) {
        if (v == null) throw new IllegalArgumentException("endedAt required");
        return Long.parseLong(v);
    }
    private Integer parseIntOrNull(String v) { return v == null ? null : Integer.parseInt(v); }
}