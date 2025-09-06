package com.smooth.driving_analysis_service.reports.behavior.entity;

import java.time.LocalTime;

public enum TimeSlot {
    DAWN("새벽", 0, 6),      // 00:00–06:00
    COMMUTE("출근", 6, 10),   // 06:00–10:00  
    DAY("낮", 10, 17),       // 10:00–17:00
    EVENING_COMMUTE("퇴근", 17, 20), // 17:00–20:00
    NIGHT("저녁", 20, 24);   // 20:00–24:00

    private final String korean;
    private final int startHour;
    private final int endHour;

    TimeSlot(String korean, int startHour, int endHour) {
        this.korean = korean;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public String getKorean() {
        return korean;
    }

    public static TimeSlot fromHour(int hour) {
        for (TimeSlot slot : values()) {
            if (hour >= slot.startHour && hour < slot.endHour) {
                return slot;
            }
        }
        return DAWN; // 기본값
    }

    public static TimeSlot from(String s) {
        return TimeSlot.valueOf(s.toUpperCase());
    }

    // 동률 우선순위: 새벽 > 낮 > 저녁 > 출근 > 퇴근
    public int getPriority() {
        switch(this) {
            case DAWN: return 1;
            case DAY: return 2;
            case NIGHT: return 3;
            case COMMUTE: return 4;
            case EVENING_COMMUTE: return 5;
            default: return 6;
        }
    }
}