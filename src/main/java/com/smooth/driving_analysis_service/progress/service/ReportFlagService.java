package com.smooth.driving_analysis_service.progress.service;

import com.smooth.driving_analysis_service.progress.domain.ReportFlag;
import com.smooth.driving_analysis_service.progress.dto.ReportBannerFlagDto;
import com.smooth.driving_analysis_service.progress.repository.UserReportFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportFlagService {

    private final UserReportFlagRepository flagRepository;

    /** 15회 달성 시 플래그 true로 설정 (멱등) */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setEligibleIfNot(String userId) {
        var now = LocalDateTime.now();
        var flag = flagRepository.findById(userId).orElseGet(() ->
                ReportFlag.builder()
                         .userId(userId)
                        .eligible(false)
                        .bannerAck(false)
                        .updatedAt(now)
                        .build()
        );

        if (!flag.isEligible()) {           // 이미 true면 아무 것도 안 함 → 멱등
            flag.setEligible(true);
            flag.setEligibleAt(now);
            flag.setUpdatedAt(now);
            flagRepository.save(flag);
        }
    }

    /** 배너 확인(닫힘) 처리 (멱등) */
    @Transactional
    public void ackBanner(String userId) {
        var now = LocalDateTime.now();
        var flag = flagRepository.findById(userId).orElseGet(() ->
                ReportFlag.builder()
                        .userId(userId)
                        .eligible(false)
                        .bannerAck(false)
                        .updatedAt(now)
                        .build()
        );
        if (!flag.isBannerAck()) {
            flag.setBannerAck(true);
            flag.setBannerAckAt(now);
            flag.setUpdatedAt(now);
            flagRepository.save(flag);
        }
    }

    /** 배너/알림 플래그 조회 (프론트 배너 노출 여부 판단용) */
    @Transactional(readOnly = true)
    public ReportBannerFlagDto getFlag(String userId) {
        return flagRepository.findById(userId)
                .map(f -> new ReportBannerFlagDto(f.isEligible(), f.isBannerAck()))
                .orElseGet(() -> new ReportBannerFlagDto(false, false));
    }
}
