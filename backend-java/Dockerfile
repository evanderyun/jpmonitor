FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -U -q

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -g 1001 jpmonitor && adduser -D -u 1001 -G jpmonitor jpmonitor
WORKDIR /app
RUN mkdir -p /app/logs && chown -R jpmonitor:jpmonitor /app/logs
COPY --from=builder /app/api/target/api-0.0.1-SNAPSHOT.jar app.jar
USER jpmonitor
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
