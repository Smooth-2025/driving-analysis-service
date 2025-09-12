package com.smooth.driving_analysis_service.reports.dna.service.impl;

import com.smooth.driving_analysis_service.reports.dna.service.DnaComputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class DnaComputeServiceImpl implements DnaComputeService {

    private static final double A_SLOW_MIN = 10.0;
    private static final double A_FAST_MAX = 5.0;

    private static final double C_CONSERV_MAX = 1.0;
    private static final double C_AGGRESS_MIN = 2.0;

    private static final long D_EARLY_MAX_MS = 2000;
    private static final long D_LATE_MIN_MS  = 5000;

    @Override
    public String toCode(String a, String b, String c, String d) {
        return a + "-" + b + "-" + c + "-" + d;
    }

    @Override
    public Map<String, Integer> toRadar(String a, String b, String c, String d) {
        return Map.of(
                "A", gradeToScore(a),
                "B", gradeToScore(b),
                "C", gradeToScore(c),
                "D", gradeToScore(d)
        );
    }

    @Override
    public Map<String, String[]> axisMeta(String a, String b, String c, String d) {
        return Map.of(
                "A", new String[]{a, switch (a) {
                    case "A1" -> "점진형 출발: 천천히 가속해 안정적이에요.";
                    case "A3" -> "빠른 스타트형: 초반 가속이 빠른 편이에요.";
                    default -> "일반형 출발: 무리하지 않는 적절한 가속이에요.";
                }},
                "B", new String[]{b, switch (b) {
                    case "B1" -> "사전 감속형: 미리 속도를 줄여 부드럽게 감속해요.";
                    case "B3" -> "급제동형: 짧은 시간/거리 내 급감속이 잦아요.";
                    default -> "반응 감속형: 상황에 맞춰 적절히 감속해요.";
                }},
                "C", new String[]{c, switch (c) {
                    case "C1" -> "보수형: 차선 변경 빈도가 낮고 안정적이에요.";
                    case "C3" -> "공격형: 차선 변경이 잦고 추월 성향이 있어요.";
                    default -> "중립형: 필요 시 적절히 차선을 변경해요.";
                }},
                "D", new String[]{d, switch (d) {
                    case "D1" -> "조기 대응형: 알림에 빠르게 명확히 반응해요.";
                    case "D3" -> "지연 대응형: 반응이 늦거나 없을 때가 있어요.";
                    default -> "상황 유지형: 큰 무리 없이 안정적으로 대응해요.";
                }}
        );
    }

    @Override
    public String headline(String a, String b, String c, String d) {
        int score = gradeToScore(a) + gradeToScore(b) + gradeToScore(c) + gradeToScore(d);
        if (score >= 320) return "적극적이며 빠른 반응형 운전자예요!";
        if (score >= 260) return "안정적이면서 필요한 순간엔 과감해요!";
        return "무리하지 않는 차분한 주행 스타일이에요!";
    }

    @Override
    public String classifyA(Double avgSec0to40) {
        if (avgSec0to40 == null) return "A2";
        if (avgSec0to40 >= A_SLOW_MIN) return "A1";
        if (avgSec0to40 <= A_FAST_MAX) return "A3";
        return "A2";
    }

    @Override
    public String classifyB(double hardBrakePerKm) {
        if (hardBrakePerKm >= 0.30) return "B3";
        if (hardBrakePerKm <= 0.10) return "B1";
        return "B2";
    }

    @Override
    public String classifyC(double laneChangePerKm /*, Double postAccel */) {
        if (laneChangePerKm <= C_CONSERV_MAX) return "C1";
        if (laneChangePerKm >= C_AGGRESS_MIN) return "C3";
        return "C2";
    }

    @Override
    public String classifyD(Long reactionMs, Boolean responded, Boolean decelOrStop, Boolean evasive) {
        boolean hasAction = Boolean.TRUE.equals(responded) &&
                (Boolean.TRUE.equals(decelOrStop) || Boolean.TRUE.equals(evasive));
        if (reactionMs != null && reactionMs <= D_EARLY_MAX_MS && hasAction) return "D1";
        if ((reactionMs != null && reactionMs >= D_LATE_MIN_MS) || !Boolean.TRUE.equals(responded)) return "D3";
        return "D2";
    }
    
    @Override
    public String code(double A, double B, double C, double D) {
        // 점수를 기반으로 등급 분류
        String a = classifyByScore(A);
        String b = classifyByScore(B);
        String c = classifyByScore(C);
        String d = classifyByScore(D);
        return a + "-" + b + "-" + c + "-" + d;
    }
    
    @Override
    public String headline(double A, double B, double C, double D) {
        double avgScore = (A + B + C + D) / 4.0;
        if (avgScore >= 80) return "적극적이며 빠른 반응형 운전자예요!";
        if (avgScore >= 65) return "안정적이면서 필요한 순간엔 과감해요!";
        return "무리하지 않는 차분한 주행 스타일이에요!";
    }

    private int gradeToScore(String grade) {
        return switch (grade.charAt(1)) {
            case '1' -> 35;
            case '3' -> 90;
            default  -> 65;
        };
    }
    
    private String classifyByScore(double score) {
        if (score >= 80) return "A3"; // 높은 점수
        if (score <= 50) return "A1"; // 낮은 점수
        return "A2"; // 중간 점수
    }
}