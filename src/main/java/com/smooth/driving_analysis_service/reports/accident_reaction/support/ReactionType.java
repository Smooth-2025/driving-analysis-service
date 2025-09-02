package com.smooth.driving_analysis_service.reports.accident_reaction.support;

public enum ReactionType {
    ACCIDENT_NEARBY("accident-nearby"),
    OBSTACLE("obstacle");

    private final String apiName;
    ReactionType(String apiName) { this.apiName = apiName; }

    public static ReactionType of(String raw) {
        if (raw == null) return null;
        String k = raw.trim().toLowerCase();
        for (var t : values()) if (t.apiName.equals(k)) return t;
        return null;
    }
}
