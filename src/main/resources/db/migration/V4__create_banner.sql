-- 배너 플래그 테이블(멱등) 생성
CREATE TABLE IF NOT EXISTS banner (
                                           user_id        VARCHAR(255) PRIMARY KEY,
    acked_reports  INT NOT NULL DEFAULT 0, -- 사용자가 '확인'한 누적 15회 리포트 개수
    updated_at     TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP
    ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

-- 컬럼 보강(이미 있을 수 있으므로)
ALTER TABLE banner
    ADD COLUMN IF NOT EXISTS acked_reports INT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS updated_at    TIMESTAMP NULL
    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
