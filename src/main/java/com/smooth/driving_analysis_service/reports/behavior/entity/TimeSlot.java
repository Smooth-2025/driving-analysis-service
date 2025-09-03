package com.smooth.driving_analysis_service.reports.behavior.entity;

import java.time.LocalTime;

public enum TimeSlot {
    MORNING, AFTERNOON, EVENING, NIGHT;

    public static TimeSlot from(String s) {
        return TimeSlot.valueOf(s.toUpperCase());
    }

    public String getKorean() {
        switch(this) {
            case MORNING: return "아침";
            case AFTERNOON: return "오후";
            case EVENING: return "저녁";
            case NIGHT: return "밤";
            default: return "-";
        }
    }
}