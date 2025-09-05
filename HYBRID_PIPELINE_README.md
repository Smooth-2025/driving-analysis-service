# 하이브리드 파이프라인 시스템

## 🎯 시스템 개요

주행 완료 이벤트를 실시간으로 처리하여 통합 통계를 저장하고, 마일스톤 기반으로 리포트를 생성하는 하이브리드 파이프라인입니다.

### 핵심 원칙
- **실시간**: XADD 메시지 + DrivingRecord 통합 → 누적 통계 저장
- **배치**: 누적 통계 기반 분석 + 스냅샷 생성

## 🏗️ 아키텍처 구성

### 패키지별 역할

| 패키지 | 역할 | 처리 시점 |
|--------|------|-----------|
| `trigger` | 마일스톤 관리 + 트리거 발행 | 주행 완료 즉시 |
| `pipeline` | XADD + DrivingRecord 통합 → 누적 통계 저장 | trigger에서 호출 |
| `batch` | 배치 스케줄링 + report.trigger 소비 | 10분 / 새벽 2시 |
| `reports/basic-summary` | 리포트 상단 요약 생성 | batch에서 호출 |
| `reports/behavior` | 행동 분석 | batch에서 호출 |
| `reports/dna` | DNA 분석 | batch에서 호출 |
| `reports/accident-reaction` | 사고 대응 분석 | batch에서 호출 |

### 데이터 저장소

| 저장소 | 용도 | 데이터 타입 |
|--------|------|-------------|
| Redis | 멱등성, 캐시, 스트림 | 임시 |
| RDS | 누적 통계, 마일스톤, 스냅샷 | 영구 |
| S3 | 상세 주행 로우 데이터 | 배치 분석용 |

## 📊 상태 및 스냅샷 관리

### MilestoneReport 상태
- **COLLECTING**: 1~14개 누적
- **PROCESSING**: 15개 달성, 최종 분석 대기/진행
- **COMPLETED**: 최종 분석 완료, 사용자 노출 가능

### 스냅샷 타입
- **INTERIM**: 4/8/12회, 미노출, 교체 가능
- **FINAL**: 15회, 노출용, 불변

## 🔄 데이터 플로우

```
다른 팀: 주행 완료 → DrivingRecord 저장 → XADD driving-analysis-stream
    ↓
DrivingSummaryConsumer (trigger)
    ↓
DrivingSummaryConsumerService (@Transactional)
    ↓
┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
│ 멱등성 체크      │ 통합 통계 저장   │ 마일스톤 관리    │ 배치 트리거      │
│ Redis TTL      │ pipeline 호출    │ milestone_item  │ report.trigger  │
│                │ XADD+DrivingRecord│ milestone_report│ 발행            │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┘
    ↓
ReportTriggerConsumer (batch) - report.trigger 스트림 소비
    ↓
BatchReportService (@Scheduled)
    ↓
┌─────────────────┬─────────────────┬─────────────────┬─────────────────┐
│ basic-summary   │ behavior 분석    │ dna 분석        │ accident 분석    │
│ 누적 통계 활용   │ + S3 데이터     │ + S3 데이터     │ + S3 데이터     │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┘
    ↓
Interim/Final 스냅샷 저장 + milestone_report.status 업데이트
```

## 🎬 상세 시나리오

### 1) 초기 주행 (1~3회)

**샘플 XADD (1회차)**
```bash
XADD driving-analysis-stream * \
  userId "12345" drivingId "trip-001" \
  startedAt "2025-01-09T10:00:00" endedAt "2025-01-09T10:30:00" \
  drivingMinutes "30" totalDistance "15000" \
  laneChangeCount "3" hardBrakeCount "1" rapidAccelCount "2"
```

**trigger 처리 결과**
- `processed:trip:trip-001 = "1"` (Redis, TTL 7일)
- `driving_accumulated_stats`: XADD + DrivingRecord 통합 저장
- `milestone_report`: COLLECTING, numberOfDriving=1
- `milestone_item`: reportId=1, drivingId=trip-001, orderNo=1

⏱ **배치 트리거 없음** (4회 미만)

### 2) 첫 중간 분석 (4회 달성)

**샘플 XADD (4회차)**
```bash
XADD driving-analysis-stream * \
  userId "12345" drivingId "trip-004" \
  startedAt "2025-01-09T16:00:00" endedAt "2025-01-09T16:35:00" \
  drivingMinutes "35" totalDistance "18000" \
  laneChangeCount "5" hardBrakeCount "1" rapidAccelCount "2"
```

**trigger 처리**
- 누적 통계 저장
- milestone_report: numberOfDriving=4
- 트리거 발행 (report.trigger)

**ReportTriggerV1 (예시)**
```json
{
  "type": "INTERIM",
  "userId": "12345",
  "reportId": 1,
  "milestone": 4,
  "status": "COLLECTING",
  "drivingIds": ["trip-001", "trip-002", "trip-003", "trip-004"]
}
```

**배치 실행 (10분 단위)**
- basic-summary: driving_accumulated_stats → basic_summary (INTERIM)
- behavior/dna/accident_reaction: driving_accumulated_stats + S3 상세 데이터 → 각 (INTERIM)

### 3) 중간 분석 반복 (8·12회)
- 8회: 기존 4회 INTERIM 스냅샷 덮어쓰기
- 12회: 기존 8회 INTERIM 스냅샷 덮어쓰기
- 누적 통계는 실시간으로 계속 저장 (1~해당 시점 전체 반영)

### 4) 최종 분석 (15회 달성)

**trigger 처리**
- 누적 통계 저장
- milestone_report: numberOfDriving=15, status=PROCESSING
- active-report 캐시 삭제
- 트리거 발행 (type=FINAL)

**배치 실행**
- basic-summary/behavior/dna/accident_reaction → 각 (FINAL)
- 상태 변경: milestone_report.status = COMPLETED (노출 가능)

### 5) 새 사이클 시작 (16회차)
- 새 milestone_report 생성 (cycleNo=2, status=COLLECTING)
- 동일 플로우 반복

## 🧪 테스트 가이드

### Redis CLI (샘플)

```bash
# 1~3회차
XADD driving-analysis-stream * v "1" userId "12345" drivingId "trip-001" startedAt "2025-01-09T10:00:00" endedAt "2025-01-09T10:30:00" status "COMPLETED" producer "test" drivingMinutes "30" totalDistance "15000" laneChangeCount "3" hardBrakeCount "1" rapidAccelCount "2"

XADD driving-analysis-stream * v "1" userId "12345" drivingId "trip-002" startedAt "2025-01-09T11:00:00" endedAt "2025-01-09T11:25:00" status "COMPLETED" producer "test" drivingMinutes "25" totalDistance "12000" laneChangeCount "2" hardBrakeCount "0" rapidAccelCount "1"

XADD driving-analysis-stream * v "1" userId "12345" drivingId "trip-003" startedAt "2025-01-09T14:00:00" endedAt "2025-01-09T14:40:00" status "COMPLETED" producer "test" drivingMinutes "40" totalDistance "20000" laneChangeCount "4" hardBrakeCount "2" rapidAccelCount "3"

# 4회차 (트리거)
XADD driving-analysis-stream * v "1" userId "12345" drivingId "trip-004" startedAt "2025-01-09T16:00:00" endedAt "2025-01-09T16:35:00" status "COMPLETED" producer "test" drivingMinutes "35" totalDistance "18000" laneChangeCount "5" hardBrakeCount "1" rapidAccelCount "2"
```

### 확인할 데이터 (SQL)

```sql
-- 누적 통계
SELECT * FROM driving_accumulated_stats WHERE user_id = 12345 ORDER BY created_at;

-- 마일스톤
SELECT * FROM milestone_report WHERE user_id = 12345;
SELECT * FROM milestone_item WHERE report_id = 1 ORDER BY order_no;

-- 스냅샷 (4회차 후)
SELECT * FROM basic_summary WHERE report_id = 1 AND snapshot_type = 'INTERIM';
SELECT * FROM behavior_summary WHERE report_id = 1 AND snapshot_type = 'INTERIM';
SELECT * FROM dna_summary WHERE report_id = 1 AND snapshot_type = 'INTERIM';
SELECT * FROM accident_reaction_summary WHERE report_id = 1 AND snapshot_type = 'INTERIM';
```

```bash
# report.trigger 스트림 확인
XREAD STREAMS report.trigger 0
```

## ⚙️ 설정 및 운영

### 배치 스케줄링 (batch)
- **개발**: 10분 — `@Scheduled(fixedRate = 600000)`
- **운영**: 새벽 2시 — `@Scheduled(cron = "0 0 2 * * *")`

### 핵심 설정값
- **임계값**: 15회 (최종)
- **중간 분석**: 4/8/12회
- **TTL**: processed:* 7일, active-report:* 30일

## 🔑 핵심 포인트 요약

### 실시간 (trigger + pipeline)
- `processed:trip:{tripId}` 멱등성
- XADD + DrivingRecord → driving_accumulated_stats
- 4/8/12/15 도달 시 report.trigger 발행
- 상태: COLLECTING → PROCESSING → COMPLETED

### 배치 (batch + reports)
- report.trigger 소비 + @Scheduled
- 누적 통계 활용 + S3 정밀 분석
- INTERIM(교체) vs FINAL(불변)
- 단계별 분석: 4 → 8 → 12 → 15

## 🧱 핵심 테이블 구조

```sql
CREATE TABLE driving_accumulated_stats (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    driving_id VARCHAR(255) NOT NULL,
    -- XADD
    driving_minutes INT,
    total_distance INT,
    lane_change_count INT,
    hard_brake_count INT,
    rapid_accel_count INT,
    -- DrivingRecord
    avg_speed DOUBLE,
    cruise_ratio DOUBLE,
    start_time DATETIME,
    end_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

## 🧾 리포트 상단 요약 API (예시)

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "리포트 상단 요약 조회 완료",
  "data": {
    "reportId": "u1_r3_20250901",
    "totalDistanceKm": 26.6,
    "periodStart": "2025-08-01",
    "periodEnd": "2025-08-28",
    "averageDurationSec": 38.25,
    "averageDistanceKm": 1.77,
    "averageSpeedKmh": 42.3,
    "averageCruiseRatio": 0.684
  }
}
```

## 📋 모니터링 체크리스트

- [ ] driving-analysis-stream 지연 (trigger)
- [ ] report.trigger 지연 (batch)
- [ ] 배치 처리 성공률 (batch)
- [ ] 스냅샷 생성 시간 (reports)
- [ ] 멱등성 위반 건수 (trigger)
- [ ] DrivingRecord 조회 실패율 (pipeline)