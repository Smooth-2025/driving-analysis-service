package com.smooth.driving_analysis_service.reports.dna.service;

import java.util.Map;

public interface DnaComputeService {
    String toCode(String a, String b, String c, String d);
    Map<String, Integer> toRadar(String a, String b, String c, String d);
    Map<String, String[]> axisMeta(String a, String b, String c, String d);
    String headline(String a, String b, String c, String d);

    String classifyA(Double avgSec0to40);
    String classifyB(double hardBrakePerKm);                 // 폴백용
    String classifyC(double laneChangePerKm /* , Double postAccel */);
    String classifyD(Long reactionMs, Boolean responded, Boolean decelOrStop, Boolean evasive);
}
