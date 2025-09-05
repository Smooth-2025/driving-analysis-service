package com.smooth.driving_analysis_service.batch.service.impl;

import com.smooth.driving_analysis_service.batch.service.StatusTransitionService;
import com.smooth.driving_analysis_service.reports.milestone.entity.MilestoneReport;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatusTransitionServiceImpl implements StatusTransitionService {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void toCompleted(Long reportId) {
        MilestoneReport r = em.find(MilestoneReport.class, reportId);
        if (r == null) return;
        if (r.getStatus() != MilestoneReport.Status.COMPLETED) {
            r.setStatus(MilestoneReport.Status.COMPLETED);
            log.info("[BATCH] report {} -> COMPLETED", reportId);
        }
    }
}
