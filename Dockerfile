# syntax=docker/dockerfile:1

############################
# 1) Build stage
############################
FROM gradle:8.8-jdk21-alpine AS build
WORKDIR /workspace
# 의존성 캐시를 살리려면 먼저 Gradle 파일들만 복사
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew --version

# 소스 복사 후 빌드
COPY . .
RUN ./gradlew clean bootJar --no-daemon

############################
# 2) Runtime stage
############################
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# 컨테이너 기본 환경 (필요 시 바꿔서 -e 로 덮어쓰기 가능)
ENV SPRING_PROFILES_ACTIVE=dev \
    SERVER_PORT=8081 \
    JAVA_OPTS=""

# 빌드 산출물 복사 (bootJar 결과물 1개)
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

# 컨테이너 외부에 노출할 포트(참고용)
EXPOSE 8081

# (원하면 Actuator health 켜서 헬스체크 추가 가능)
# HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD wget -qO- http://localhost:${SERVER_PORT}/actuator/health || exit 1

# 실행
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -jar /app/app.jar"]
