-- 안전 정리
DROP TABLE IF EXISTS v_user_progress_15;
DROP VIEW  IF EXISTS v_user_progress_15;

CREATE VIEW v_user_progress_15 AS
SELECT
    s.user_id                                                    AS user_id,
    COUNT(s.trip_id)                                             AS total_trips,
    15                                                           AS threshold,
    CASE WHEN (COUNT(s.trip_id) % 15) = 0 THEN 0
         ELSE 15 - (COUNT(s.trip_id) % 15) END                   AS remaining_trips,
    -- ✅ remaining_trips의 반대 (이번 사이클에서 채운 개수)
    CASE
        WHEN COUNT(s.trip_id) = 0 THEN 0
        WHEN (COUNT(s.trip_id) % 15) = 0 THEN 15
        ELSE (COUNT(s.trip_id) % 15)
        END                                                          AS current_cycle_count
FROM trip_summary s
GROUP BY s.user_id;
