package com.smooth.driving_analysis_service.progress.repository;

import com.smooth.driving_analysis_service.progress.dto.ProgressDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProgressQueryRepository {

    private final JdbcTemplate jdbc;

    public Optional<ProgressDto> findProgress(String userId) {
        String sql = """
            SELECT user_id, total_trips, threshold, remaining_trips, reports_generated
            FROM v_user_progress_15
            WHERE user_id = ?
            """;

        return jdbc.query(sql, rs -> {
            if (!rs.next()) return Optional.empty();
            return Optional.of(
                    ProgressDto.builder()
                            .userId(rs.getString("user_id"))
                            .totalTrips(rs.getInt("total_trips"))
                            .threshold(rs.getInt("threshold"))
                            .remainingTrips(rs.getInt("remaining_trips"))
                            .reportsGenerated(rs.getInt("reports_generated"))
                            .build()
                            );
        }, userId);
    }
}