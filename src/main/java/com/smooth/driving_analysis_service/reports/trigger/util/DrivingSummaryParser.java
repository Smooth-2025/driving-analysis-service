package com.smooth.driving_analysis_service.reports.trigger.util;

import com.smooth.driving_analysis_service.reports.trigger.dto.DrivingSummaryV1;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.util.Map;
import java.util.function.Function;

public final class DrivingSummaryParser {

    private DrivingSummaryParser() {}

    public static DrivingSummaryV1 parse(MapRecord<String, Object, Object> record) {
        Map<Object, Object> m = record.getValue();

        DrivingSummaryV1 dto = new DrivingSummaryV1();
        dto.setV(intOf(m, "v", 1));
        dto.setUserId(strOf(m, "userId"));
        dto.setDrivingId(strOf(m, "drivingId"));

        // 시간 필드: DrivingEventDto 기준(startTime/endTime) + 백워드 호환(startedAt/endedAt)
        // 모두 epoch millis(Long)로 변환
        Long startedAt = epochMillisOf(m, "startTime");
        if (startedAt == null) startedAt = longOf(m, "startedAt");
        dto.setStartedAt(startedAt != null ? startedAt.toString() : null);

        Long endedAt = epochMillisOf(m, "endTime");
        if (endedAt == null) endedAt = longOf(m, "endedAt");
        dto.setEndedAt(endedAt != null ? endedAt.toString() : "0");

        dto.setStatus(strOf(m, "status"));
        dto.setProducer(strOf(m, "producer")); // 없어도 OK

        // 통계 필드 매핑 (DrivingEventDto 키명 우선)
        dto.setDrivingMinutes(intOf(m, "drivingMinutes", (Void v) -> {
            Integer s = intOf(m, "durationS", -1);
            return (s != null && s > 0) ? s / 60 : null;
        }));

        Double totalDistanceDouble = doubleFirst(m,
                "totalDistance",       // DrivingEventDto
                "distanceM"            // 백워드 호환
        );
        dto.setTotalDistance(totalDistanceDouble != null ? totalDistanceDouble.intValue() : null);

        dto.setLaneChangeCount(intFirst(m,
                "laneChangeCount",     // DrivingEventDto
                "evLaneChange"         // 백워드 호환
        ));

        dto.setHardBrakeCount(intFirst(m,
                "hardBrakeCount",      // DrivingEventDto
                "evHardBrake"          // 백워드 호환
        ));

        dto.setRapidAccelCount(intFirst(m,
                "rapidAccelCount",     // DrivingEventDto
                "evRapidAccel"         // 백워드 호환
        ));

        // 필수값 검증은 컨슈머에서 dto.validateForProcessing()로 수행
        return dto;
    }

    /* ===================== helpers ===================== */

    private static String strOf(Map<Object, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        return s.isEmpty() ? null : s;
    }

    private static Integer intFirst(Map<Object, Object> m, String primary, String fallback) {
        Integer a = intOrNull(m, primary);
        if (a != null) return a;
        return intOrNull(m, fallback);
    }

    private static Double doubleFirst(Map<Object, Object> m, String primary, String fallback) {
        Double a = doubleOrNull(m, primary);
        if (a != null) return a;
        return doubleOrNull(m, fallback);
    }

    private static Integer intOf(Map<Object, Object> m, String key, int def) {
        Integer v = intOrNull(m, key);
        return v != null ? v : def;
    }

    private static Integer intOf(Map<Object, Object> m, String key, Function<Void, Integer> fallbackSupplier) {
        Integer v = intOrNull(m, key);
        if (v != null) return v;
        return fallbackSupplier != null ? fallbackSupplier.apply(null) : null;
    }

    private static Integer intOrNull(Map<Object, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        try {
            String s = v.toString().trim();
            if (s.isEmpty()) return null;
            return Integer.parseInt(s);
        } catch (Exception ignore) { return null; }
    }

    private static Double doubleOrNull(Map<Object, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        try {
            String s = v.toString().trim();
            if (s.isEmpty()) return null;
            return Double.parseDouble(s);
        } catch (Exception ignore) { return null; }
    }

    private static Long longOf(Map<Object, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        try {
            String s = v.toString().trim();
            if (s.isEmpty()) return null;
            return Long.parseLong(s);
        } catch (Exception ignore) { return null; }
    }

    /**
     * value가 ISO-8601(LocalDateTime) 문자열일 수도 있고, epoch millis일 수도 있다고 가정.
     * - 숫자면 Long으로 파싱
     * - 문자열이면 ISO-8601을 epoch millis로 변환 시도 (실패 시 null)
     */
    private static Long epochMillisOf(Map<Object, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return null;
        String s = v.toString().trim();
        if (s.isEmpty()) return null;

        // 숫자형이면 그대로
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException ignore) {}

        // ISO-8601 → epoch millis
        try {
            // java.time.LocalDateTime.parse(s) 는 timezone 없음 → 시스템 offset 적용 필요
            java.time.LocalDateTime ldt = java.time.LocalDateTime.parse(s);
            return ldt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception ignore) {}

        return null;
    }
}
