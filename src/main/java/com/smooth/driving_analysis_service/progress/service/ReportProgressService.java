package com.smooth.driving_analysis_service.progress.service;

import com.smooth.driving_analysis_service.progress.dto.ReportEligibilityDto;
import com.smooth.driving_analysis_service.progress.dto.ReportProgressDto;
import com.smooth.driving_analysis_service.progress.repository.DrivingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportProgressService {

    private static final int THRESHOLD = 15;

    // ✅ 의미에 맞게 필드명 정리 + final 로 주입
    private final DrivingRecordRepository drivingRecordRepository;

    @Transactional(readOnly = true)
    public ReportProgressDto getProgress(String rawUserId) {
        // ✅ 숫자 "9"도 지원하려면 정규화 유지 (불필요하면 바로 rawUserId 사용)
        String userId = normalizeUserId(rawUserId);

        long total = drivingRecordRepository.countByUserId(userId);
        long remaining = Math.max(0, THRESHOLD - total);
        double progress = Math.min(1.0d, (double) total / THRESHOLD);

        return ReportProgressDto.builder()
                .totalTrips(total)
                .threshold(THRESHOLD)
                .remainingToThreshold(remaining)
                .progressRate(progress)
                .build();
    }

    @Transactional(readOnly = true)
    public ReportEligibilityDto getEligibility(String rawUserId) {
        // ✅ 동일 로직 일관화
        String userId = normalizeUserId(rawUserId);

        long total = drivingRecordRepository.countByUserId(userId);
        boolean eligible = total >= THRESHOLD;
        long remaining = Math.max(0, THRESHOLD - total);

        return ReportEligibilityDto.builder()
                .totalTrips(total)
                .threshold(THRESHOLD)
                .isEligible(eligible)
                .remainingTrips(remaining)
                .build();
    }

    /** 숫자 "9" -> "user9", " USER9 " -> "user9" (숫자도 허용하려면 유지, 아니면 메서드 제거) */
    private String normalizeUserId(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toLowerCase();
        if (s.matches("^\\d+$")) {
            return "user" + s;
        }
        return s;
    }
}
