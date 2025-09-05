package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.service.DrivingIdResolverService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DrivingIdResolverImpl implements DrivingIdResolverService {

    private final EntityManager em;

    @Value("${accident.reaction.resolve.bufferSec:300}")
    private int bufferSec;

    @Override
    @Transactional(readOnly = true)
    public String resolve(long userId, LocalDateTime t) {
        // 1) 포함 검색
        List<String> exact = em.createQuery("""
            select r.drivingId
              from DrivingRecord r
             where r.userId = :u
               and r.startTime <= :t
               and r.endTime   >= :t
             order by (r.endTime - r.startTime) asc
        """, String.class)
                .setParameter("u", userId)
                .setParameter("t", t)
                .setMaxResults(1)
                .getResultList();

        if (!exact.isEmpty()) {
            log.info("[사고반응] drivingId 확인(포함 일치): userId={}, renderedAt={}, drivingId={}", userId, t, exact.get(0));
            return exact.get(0);
        }

        // 2) 근접 검색(옵션)
        LocalDateTime from = t.minusSeconds(bufferSec);
        LocalDateTime to   = t.plusSeconds(bufferSec);
        List<String> near = em.createQuery("""
            select r.drivingId
              from DrivingRecord r
             where r.userId = :u
               and (r.startTime between :f and :to
                 or r.endTime   between :f and :to)
             order by abs(timestampdiff(second, r.startTime, :t)) asc
        """, String.class)
                .setParameter("u", userId)
                .setParameter("f", from)
                .setParameter("to", to)
                .setParameter("t", t)
                .setMaxResults(1)
                .getResultList();

        if (!near.isEmpty()) {
            log.info("[사고반응] drivingId 확인(근접 일치): userId={}, renderedAt={}, drivingId={}", userId, t, near.get(0));
            return near.get(0);
        }

        log.warn("[사고반응] drivingId 매칭 실패: userId={}, renderedAt={}", userId, t);
        return null;
    }
}
