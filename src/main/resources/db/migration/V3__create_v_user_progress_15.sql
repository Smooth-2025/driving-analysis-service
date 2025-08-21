-- v_user_progress_15: reports_generated 컬럼 추가
CREATE OR REPLACE VIEW v_user_progress_15 AS
SELECT
    s.user_id                                                    AS user_id,
    COUNT(s.trip_id)                                             AS total_trips,
    15                                                           AS threshold,
    CASE
        WHEN (COUNT(s.trip_id) % 15) = 0 THEN 0
        ELSE 15 - (COUNT(s.trip_id) % 15)
        END                                                          AS remaining_trips,
    CASE
        WHEN COUNT(s.trip_id) = 0 THEN 0
        WHEN (COUNT(s.trip_id) % 15) = 0 THEN 15
        ELSE (COUNT(s.trip_id) % 15)
        END                                                          AS current_cycle_count,
    FLOOR(COUNT(s.trip_id) / 15)                                 AS reports_generated
FROM trip_summary s
GROUP BY s.user_id;
