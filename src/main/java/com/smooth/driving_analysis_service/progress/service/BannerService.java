package com.smooth.driving_analysis_service.progress.service;

import com.smooth.driving_analysis_service.progress.dto.BannerDto;
import com.smooth.driving_analysis_service.progress.domain.BannerDomain;
import com.smooth.driving_analysis_service.progress.domain.VUserProgress15Domain;
import com.smooth.driving_analysis_service.progress.repository.BannerRepository;
import com.smooth.driving_analysis_service.progress.repository.VUserProgress15Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final VUserProgress15Repository progressRepo;
    private final BannerRepository bannerRepo;

    @Transactional(readOnly = true)
    public BannerDto getStatus(String rawUserId) {
        String userId = normalize(rawUserId);

        VUserProgress15Domain p = progressRepo.findById(userId).orElse(null);
        int threshold        = 15;
        int generated        = p == null ? 0 : nz(p.getReportsGenerated());
        int acked            = bannerRepo.findById(userId)
                .map(BannerDomain::getAckedReports).orElse(0);
        boolean show         = generated > acked;
        int pending          = Math.max(0, generated - acked);

        return BannerDto.builder()
                .userId(userId)
                .showBanner(show)
                .reportsGenerated(generated)
                .ackedReports(acked)
                .pendingReports(pending)
                .threshold(threshold)
                .build();
    }

    @Transactional
    public BannerDto ack(String rawUserId) {
        String userId = normalize(rawUserId);

        VUserProgress15Domain p = progressRepo.findById(userId).orElse(null);
        int generated = p == null ? 0 : nz(p.getReportsGenerated());

        // 멱등 upsert (보고서 개수는 더 큰 값으로 고정)
        bannerRepo.upsertAck(userId, generated);

        return getStatus(userId);
    }

    private int nz(Integer v) { return v == null ? 0 : v; }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.trim();
        return t.toLowerCase();
    }
}
