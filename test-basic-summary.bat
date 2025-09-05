@echo off
echo "=== Basic Summary API 테스트 실행 ==="
echo.

echo "1. 컨트롤러 테스트 실행..."
call gradlew test --tests "*BasicSummaryControllerTest*" --no-daemon

echo.
echo "2. 서비스 테스트 실행..."
call gradlew test --tests "*BasicSummaryServiceTest*" --no-daemon

echo.
echo "3. 통합 테스트 실행..."
call gradlew test --tests "*BasicSummaryIntegrationTest*" --no-daemon

echo.
echo "=== 테스트 완료 ==="
pause