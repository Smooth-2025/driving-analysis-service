package com.smooth.driving_analysis_service.timeline.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeLineResponseDto {
    private List<TimeLineItem> items;
    private String nextCursor;
    private boolean hasMore;


    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeLineItem {
        private String id;
        private String type;
        private LocalDateTime createdAt;
        private String status;
        private Object data;
    }
}