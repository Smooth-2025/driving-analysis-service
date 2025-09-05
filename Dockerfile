FROM eclipse-temurin:21-jre-alpine
LABEL description="Docker image for driving-analysis service"
EXPOSE 8080
COPY build/libs/driving-analysis-service-0.0.1-SNAPSHOT.jar app.jar
CMD ["java", "-jar", "/app.jar"]
