# Multi-stage Dockerfile for PulseVenue Java 21 Backend (Owner: DevOps - ISS-01)
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app/backend

COPY src ./src
RUN mkdir -p target/classes && \
    javac -d target/classes $(find src/main/java -name "*.java") && \
    jar --create --file target/pulsevenue-backend.jar --main-class com.pulsevenue.PulseVenueApplication -C target/classes .

FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app
COPY --from=builder /app/backend/target/pulsevenue-backend.jar ./app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
