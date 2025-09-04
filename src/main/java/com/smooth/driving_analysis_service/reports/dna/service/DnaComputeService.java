package com.smooth.driving_analysis_service.reports.dna.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DnaComputeService {

    /**
     * A축 분류: 가속 성향 (0-40km/h 가속 시간 기반)
     */
    public String classifyA(Double sec0to40) {
        if (sec0to40 == null) return "A2"; // 기본값
        
        if (sec0to40 <= 4.0) return "A3"; // 급가속형
        if (sec0to40 >= 8.0) return "A1"; // 완만가속형
        return "A2"; // 중간
    }

    /**
     * B축 분류: 감속 성향 (하드브레이크/km 기반)
     */
    public String classifyB(double hardBrakePerKm) {
        if (hardBrakePerKm >= 0.5) return "B3"; // 급감속형
        if (hardBrakePerKm <= 0.1) return "B1"; // 사전감속형
        return "B2"; // 중간
    }

    /**
     * C축 분류: 차선변경 성향
     */
    public String classifyC(double laneChangePerKm) {
        if (laneChangePerKm >= 2.0) return "C3"; // 적극형
        if (laneChangePerKm <= 0.5) return "C1"; // 신중형
        return "C2"; // 중간
    }

    /**
     * D축 분류: 사고 대응 능력
     */
    public String classifyD(Long reactionMs, Boolean responded, Boolean decel, Boolean evasive) {
        if (reactionMs == null || responded == null) return "D2"; // 기본값
        
        if (!responded) return "D1"; // 미대응
        
        if (reactionMs <= 1000 && Boolean.TRUE.equals(decel) && Boolean.TRUE.equals(evasive)) {
            return "D3"; // 우수
        }
        
        return "D2"; // 보통
    }

    /**
     * DNA 코드 생성 (예: A2B1C3D2)
     */
    public String toCode(String A, String B, String C, String D) {
        return A + B + C + D;
    }

    /**
     * 레이더 차트용 점수 변환
     */
    public Map<String, Integer> toRadar(String A, String B, String C, String D) {
        Map<String, Integer> radar = new HashMap<>();
        radar.put("A", extractScore(A));
        radar.put("B", extractScore(B));
        radar.put("C", extractScore(C));
        radar.put("D", extractScore(D));
        return radar;
    }

    /**
     * 헤드라인 생성
     */
    public String headline(String A, String B, String C, String D) {
        StringBuilder sb = new StringBuilder();
        
        // A축 해석
        switch (A) {
            case "A1": sb.append("완만한 가속"); break;
            case "A2": sb.append("적당한 가속"); break;
            case "A3": sb.append("빠른 가속"); break;
        }
        
        sb.append(" · ");
        
        // B축 해석
        switch (B) {
            case "B1": sb.append("사전 감속"); break;
            case "B2": sb.append("적당한 감속"); break;
            case "B3": sb.append("급감속"); break;
        }
        
        sb.append(" 성향");
        
        return sb.toString();
    }

    private Integer extractScore(String classification) {
        if (classification == null || classification.length() < 2) return 2;
        
        char scoreChar = classification.charAt(1);
        return Character.getNumericValue(scoreChar);
    }
}