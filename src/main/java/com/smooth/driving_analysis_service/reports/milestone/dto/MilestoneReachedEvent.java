package com.smooth.driving_analysis_service.reports.milestone.dto;

/**
 * T7.2.1: 15회 배수 도달 시 발행되는 도메인 이벤트
 * - T7.2.2/7.2.3에서 이 이벤트를 구독하여 실제 생성/아이템 적재 수행
 */
public record MilestoneReachedEvent(Long userId, int milestone) {}
