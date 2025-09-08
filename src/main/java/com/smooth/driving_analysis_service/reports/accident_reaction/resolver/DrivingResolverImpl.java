package com.smooth.driving_analysis_service.reports.accident_reaction.resolver;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrivingResolverImpl implements DrivingResolver {
    
    @PersistenceContext 
    private EntityManager em;

    @Override
    public String resolveDrivingId(Long userId, long renderedAtMs, int windowSec) {
        log.debug("Resolving drivingId for userId: {}, renderedAt: {}, window: {}s", 
                userId, renderedAtMs, windowSec);
        
        LocalDateTime ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(renderedAtMs), ZoneId.of("Asia/Seoul"));
        log.debug("Converted timestamp: {}", ts);
        
        // 1. 정확한 시간 범위 내 주행 기록 찾기
        var inRange = em.createQuery("""
            select d.drivingId from DrivingRecord d
             where d.userId=:uid and d.startTime <= :ts and d.endTime >= :ts
             order by d.startTime desc
        """, String.class).setParameter("uid", userId).setParameter("ts", ts).setMaxResults(1).getResultList();
        
        if (!inRange.isEmpty()) {
            log.debug("Found exact match drivingId: {} for userId: {}", inRange.get(0), userId);
            return inRange.get(0);
        }
        
        log.debug("No exact match found, searching within window of {}s", windowSec);
        
        // 2. 윈도우 범위 내 가장 가까운 주행 기록 찾기
        LocalDateTime fromTime = ts.minusSeconds(windowSec);
        LocalDateTime toTime = ts.plusSeconds(windowSec);
        log.debug("Searching from {} to {}", fromTime, toTime);
        
        var near = em.createQuery("""
            select d.drivingId from DrivingRecord d
             where d.userId=:uid and (d.startTime between :from and :to or d.endTime between :from and :to)
             order by abs(timestampdiff(second, d.startTime, :ts)) asc
        """, String.class)
                .setParameter("uid", userId)
                .setParameter("from", fromTime)
                .setParameter("to", toTime)
                .setParameter("ts", ts)
                .setMaxResults(1).getResultList();
        
        String result = near.isEmpty() ? null : near.get(0);
        
        if (result == null) {
            log.warn("No drivingId found for userId: {} within {}s window of {}", userId, windowSec, ts);
            
            // 디버깅을 위해 해당 사용자의 최근 주행 기록 몇 개 조회
            var recentDriving = em.createQuery("""
                select d.drivingId, d.startTime, d.endTime from DrivingRecord d
                 where d.userId=:uid
                 order by d.startTime desc
            """, Object[].class).setParameter("uid", userId).setMaxResults(5).getResultList();
            
            log.debug("Recent driving records for userId {}: {}", userId, recentDriving);
        } else {
            log.debug("Found nearby drivingId: {} for userId: {}", result, userId);
        }
        
        return result;
    }
}
