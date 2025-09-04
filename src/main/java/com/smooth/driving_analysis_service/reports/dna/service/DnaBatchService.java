package com.smooth.driving_analysis_service.reports.dna.service;

import com.smooth.driving_analysis_service.reports.dna.entity.DnaSnapshot;

public interface DnaBatchService {
    DnaSnapshot runInterim(Long reportId);
    DnaSnapshot runFinal(Long reportId);
}
