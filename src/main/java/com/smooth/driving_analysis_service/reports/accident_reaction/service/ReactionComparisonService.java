package com.smooth.driving_analysis_service.reports.accident_reaction.service;

import com.smooth.driving_analysis_service.reports.accident_reaction.dto.response.ReactionComparisonResponseDto;

public interface ReactionComparisonService {
    ReactionComparisonResponseDto getReactionComparison(Long userId);
}