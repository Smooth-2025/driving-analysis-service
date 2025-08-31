-- V7_3_1__create_user_cycle_accumulator.sql
-- 목적: US7.3 T7.3.1 진행도/도달 판정용 누적 테이블 생성

CREATE TABLE IF NOT EXISTS cycle_accumulator (
                                                      user_id         BIGINT       NOT NULL PRIMARY KEY,      -- 유저별 1행
                                                      cycle_index     INT          NOT NULL DEFAULT 1,        -- 현재 사이클 번호(1부터)
                                                      trip_count      TINYINT UNSIGNED NOT NULL DEFAULT 0,    -- 0..15
                                                      first_trip_id   VARCHAR(64)      NULL,                  -- 해당 사이클 첫 트립
    last_trip_id    VARCHAR(64)      NULL,                  -- 해당 사이클 마지막 트립
    rapid_sum       INT          NOT NULL DEFAULT 0,        -- 누적 급가속
    hard_sum        INT          NOT NULL DEFAULT 0,        -- 누적 급제동
    lane_sum        INT          NOT NULL DEFAULT 0,        -- 누적 차선변경
    turn_sum        INT          NOT NULL DEFAULT 0,        -- 누적 급회전
    distance_m      BIGINT       NOT NULL DEFAULT 0,        -- 누적 이동거리(m)
    duration_s      BIGINT       NOT NULL DEFAULT 0,        -- 누적 주행시간(s)
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_trip_count CHECK (trip_count BETWEEN 0 AND 15)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 조회/정렬 최적화(선택)
CREATE INDEX IF NOT EXISTS idx_cycle_accu_updated_at
    ON cycle_accumulator (updated_at);
