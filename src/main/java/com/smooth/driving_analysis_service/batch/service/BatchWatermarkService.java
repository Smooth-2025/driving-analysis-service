package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.batch.entity.BatchWatermark;
import com.smooth.driving_analysis_service.batch.repository.BatchWatermarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BatchWatermarkService {

    private final BatchWatermarkRepository repo;

    @Transactional(readOnly = true)
    public boolean alreadyProcessed(LocalDate asOf) {
        BatchWatermark wm = repo.getSingleton();
        return wm != null
                && wm.getLastProcessedDate() != null
                && !asOf.isAfter(wm.getLastProcessedDate());
    }

    @Transactional
    public void markProcessed(LocalDate asOf) {
        BatchWatermark wm = repo.getSingleton();
        if (wm == null) {
            wm = BatchWatermark.builder().id(1L).lastProcessedDate(asOf).build();
        } else {
            wm.setLastProcessedDate(asOf);
        }
        repo.save(wm);
    }
}
