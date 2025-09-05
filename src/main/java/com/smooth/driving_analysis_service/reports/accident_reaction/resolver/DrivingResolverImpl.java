// reports/accident_reaction/resolver/DrivingResolverImpl.java
package com.smooth.driving_analysis_service.reports.accident_reaction.resolver;

import jakarta.persistence.EntityManager; import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor; import org.springframework.stereotype.Component;
import java.time.*;

@Component @RequiredArgsConstructor
public class DrivingResolverImpl implements DrivingResolver {
    @PersistenceContext private final EntityManager em;

    @Override
    public String resolveDrivingId(Long userId, long renderedAtMs, int bufferSec) {
        LocalDateTime ts = LocalDateTime.ofInstant(Instant.ofEpochMilli(renderedAtMs), ZoneId.of("Asia/Seoul"));
        var inRange = em.createQuery("""
            select d.drivingId from DrivingRecord d
             where d.userId=:uid and d.startTime <= :ts and d.endTime >= :ts
             order by d.startTime desc
        """, String.class).setParameter("uid", userId).setParameter("ts", ts).setMaxResults(1).getResultList();
        if (!inRange.isEmpty()) return inRange.get(0);

        var near = em.createQuery("""
            select d.drivingId from DrivingRecord d
             where d.userId=:uid and (d.startTime between :from and :to or d.endTime between :from and :to)
             order by abs(timestampdiff(second, d.startTime, :ts)) asc
        """, String.class)
                .setParameter("uid", userId)
                .setParameter("from", ts.minusSeconds(bufferSec))
                .setParameter("to", ts.plusSeconds(bufferSec))
                .setParameter("ts", ts)
                .setMaxResults(1).getResultList();
        return near.isEmpty() ? null : near.get(0);
    }
}
