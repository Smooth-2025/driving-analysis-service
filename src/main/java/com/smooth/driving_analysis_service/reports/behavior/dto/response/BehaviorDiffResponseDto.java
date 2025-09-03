package com.smooth.driving_analysis_service.reports.behavior.dto.response;

import com.smooth.driving_analysis_service.reports.behavior.entity.BehaviorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class BehaviorDiffResponseDto {
    public enum Direction { INCREASE, DECREASE, FLAT }

    private BehaviorType behavior;
    private int prev;       // 이전 총합
    private int curr;       // 이번 총합
    private int diff;       // curr - prev
    private String pct;     // prev=0 → "-" , else "xx.x%"
    private Direction direction;
    private String topChangeLabel; // "(요일×시간대) 최대 변화" 한줄 설명 (선택)
}
