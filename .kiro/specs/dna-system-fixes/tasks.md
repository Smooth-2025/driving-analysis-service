# 구현 계획

- [x] 1. DnaComputeServiceImpl 문법 오류 수정
  - axisMeta 메서드의 중복 메서드 선언 제거
  - 적절한 메서드 시그니처를 위한 Java 문법 수정
  - 모든 메서드 매개변수가 적절히 처리되도록 보장
  - _요구사항: 1.1, 1.2, 1.3_

- [x] 2. AccidentReactionMetric 엔티티 필드 매핑 수정
  - 적절한 필드 접근을 위한 getter 메서드 추가 (getResponded, getDecelOrStop, getEvasiveManeuver)
  - reactionMs 필드의 Integer to Long 변환 처리
  - 올바른 필드 접근 패턴을 사용하도록 DnaBatchServiceImpl 업데이트
  - _요구사항: 2.1, 2.3_

- [x] 3. 누락된 repository 메서드 추가
  - MilestoneItemRepository에서 findAllByReportIdOrderByOrderNoAsc 구현
  - 필요시 DrivingRecordRepository에 findByDrivingIdIn 메서드 추가
  - DnaBatchServiceImpl의 repository 메서드 호출 수정
  - _요구사항: 2.2_

- [x] 4. 야간 배치 처리 규칙 구현
  - DnaSnapshot 엔티티에 메타데이터 필드 추가 (lastInterimCount, lastInterimAt)
  - DnaBatchServiceImpl에 4/8/12 Interim 처리 로직 구현
  - 메타데이터 추적을 사용한 중복 방지 로직 추가
  - 정확히 15개 주행 기록에서 Final 처리 구현
  - _요구사항: 3.1, 3.2, 3.3, 3.4, 3.5_

- [x] 5. 적절한 상태 전환으로 배치 서비스 업데이트
  - 15개 기록에서 COLLECTING → PROCESSING 전환 구현

  - Final 스냅샷 후 PROCESSING → COMPLETED 전환 구현
  - 배치 처리 실패에 대한 적절한 오류 처리 추가
  - _요구사항: 4.1, 4.2, 4.3_

- [ ] 6. DNA 시스템 수정사항 테스트 및 검증
  - 수정된 DnaComputeService 메서드에 대한 단위 테스트 작성
  - 다른 주행 기록 수(1-3, 4, 8, 12, 15)로 배치 처리 로직 테스트
  - API 응답이 예상 형식과 일치하는지 검증
  - 종단간 DNA 분석 워크플로우 테스트
  - _요구사항: 5.1, 5.2, 5.3_