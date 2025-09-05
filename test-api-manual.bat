@echo off
echo "=== Basic Summary API 수동 테스트 ==="
echo.

set BASE_URL=http://localhost:8080
set REPORT_ID=1

echo "1. 정상 케이스 테스트..."
curl -X GET "%BASE_URL%/api/driving-analysis/reports/%REPORT_ID%/basic-summary" ^
     -H "Content-Type: application/json" ^
     -w "\nStatus: %%{http_code}\n"

echo.
echo "2. 존재하지 않는 reportId 테스트..."
curl -X GET "%BASE_URL%/api/driving-analysis/reports/999/basic-summary" ^
     -H "Content-Type: application/json" ^
     -w "\nStatus: %%{http_code}\n"

echo.
echo "3. 잘못된 reportId 형식 테스트..."
curl -X GET "%BASE_URL%/api/driving-analysis/reports/invalid/basic-summary" ^
     -H "Content-Type: application/json" ^
     -w "\nStatus: %%{http_code}\n"

echo.
echo "=== 수동 테스트 완료 ==="
pause