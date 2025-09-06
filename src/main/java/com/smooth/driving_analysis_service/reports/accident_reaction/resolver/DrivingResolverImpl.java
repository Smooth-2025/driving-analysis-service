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
        var inRange = em.createQuery("""
            select d.drivingId from DrivingRecord d
             where d.userId=:uid and d.startTime <= :ts and d.endTime >= :ts
             order by d.startTime desc
        """, String.class).setParameter("uid", userId).setParameter("ts", ts).setMaxResults(1).getResultList();
        if (!inRange.isEmpty()) {
            log.debug("Resolved drivingId: {} for userId: {}", inRange.get(0), userId);
            return inRange.get(0);
        }

        var near = em.createQuery("""
            select d.drivingId from DrivingRecord d
             where d.userId=:uid and (d.startTime between :from and :to or d.endTime between :from and :to)
             order by abs(timestampdiff(second, d.startTime, :ts)) asc
        """, String.class)
                .setParameter("uid", userId)
                .setParameter("from", ts.minusSeconds(windowSec))
                .setParameter("to", ts.plusSeconds(windowSec))
                .setParameter("ts", ts)
                .setMaxResults(1).getResultList();
        
        String result = near.isEmpty() ? null : near.get(0);
        log.debug("Resolved drivingId: {} for userId: {}", result, userId);
        return result;
    }
}
