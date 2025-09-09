package com.smooth.driving_analysis_service.reports.batch.service;

import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReportFinderServiceImpl implements ReportFinderService {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<MilestoneReport> findReports(MilestoneReport.Status status) {
        return em.createQuery("""
                select r
                  from MilestoneReport r
                 where r.status = :status
                """, MilestoneReport.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public int countItems(Long reportId) {
        Long c = em.createQuery("""
                select count(i)
                  from MilestoneItem i
                 where i.reportId = :rid
                """, Long.class)
                .setParameter("rid", reportId)
                .getSingleResult();
        return c == null ? 0 : c.intValue();
    }
}
