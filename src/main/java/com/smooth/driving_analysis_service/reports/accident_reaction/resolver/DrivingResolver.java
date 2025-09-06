package com.smooth.driving_analysis_service.reports.accident_reaction.resolver;

public interface DrivingResolver {
    
    /**
     * 주어진 시간과 사용자 ID를 기반으로 해당하는 주행 ID를 찾습니다.
     * 
     * @param userId 사용자 ID
     * @param renderedAtMs 알림 발생 시간 (밀리초)
     * @param windowSec 검색 윈도우 (초)
     * @return 해당하는 주행 ID
     */
    String resolveDrivingId(Long userId, long renderedAtMs, int windowSec);
}