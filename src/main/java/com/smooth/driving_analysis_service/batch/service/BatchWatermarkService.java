package com.smooth.driving_analysis_service.batch.service;

import com.smooth.driving_analysis_service.batch.entity.BatchWatermark;
import com.smooth.driving_analysis_service.batch.repository.BatchWatermarkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BatchWatermarkService {

    private final BatchWatermarkRepository repo;

    public boolean alreadyProcessed(LocalDate asOf) {
        BatchWatermark wm = repo.getSingleton();
        return wm != null
                && wm.getLastProcessedDate() != null
                && !asOf.isAfter(wm.getLastProcessedDate());
    }

    public void markDone(LocalDate asOf) {
        BatchWatermark wm = repo.findById(1).orElseGet(BatchWatermark::new);
        wm.setId(1);
        wm.setLastProcessedDate(asOf);
        wm.setUpdatedAt(LocalDateTime.now());
        repo.save(wm);
    }
}
