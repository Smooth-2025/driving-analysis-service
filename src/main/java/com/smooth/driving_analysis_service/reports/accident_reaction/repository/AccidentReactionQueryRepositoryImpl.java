package com.smooth.driving_analysis_service.reports.accident_reaction.repository;

import com.smooth.driving_analysis_service.reports.accident_reaction.entity.AccidentReactionMetric;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AccidentReactionQueryRepositoryImpl implements AccidentReactionQueryRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<AccidentReactionMetric> findLatestByDrivingIds(List<String> drivingIds, int limitPerDriving) {
        if (drivingIds == null || drivingIds.isEmpty()) return List.of();
        // 간단 버전: drivingIds 전체에서 최신 순 N*limitPerDriving개 가져오고 애플리케이션 레벨에서 필터
        return em.createQuery("""
                SELECT a
                FROM AccidentReactionMetric a
                WHERE a.drivingId IN :ids
                ORDER BY a.createdAt DESC
                """, AccidentReactionMetric.class)
                .setParameter("ids", drivingIds)
                .setMaxResults(Math.max(50, drivingIds.size() * Math.max(limitPerDriving, 1)))
                .getResultList();
    }
}
