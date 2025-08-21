-- 안전 정리
DROP TABLE IF EXISTS v_user_progress_15;
DROP VIEW  IF EXISTS v_user_progress_15;

-- VIEW 생성
CREATE VIEW v_user_progress_15 AS
SELECT
    s.user_id                                   AS user_id,
    COUNT(s.trip_id)                            AS total_trips,
    15                                          AS threshold,
    CASE WHEN (COUNT(s.trip_id) % 15) = 0
             THEN 0 ELSE 15 - (COUNT(s.trip_id) % 15) END AS remaining_trips
FROM trip_summary s
GROUP BY s.user_id;
