FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /build

# Copy POM first (layer cache — dependencies re-downloaded only if pom.xml changes)
COPY backend/pom.xml ./pom.xml
RUN mvn dependency:go-offline -q

# Copy source
COPY backend/src ./src

# Build fat JAR (skip tests for image build — run them separately with: mvn test)
RUN mvn package -DskipTests -q

# ── Runtime image ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /build/target/service-booking-platform-1.0.0.jar app.jar

# The CLI reads from stdin; always run with: docker run -it <image>
ENTRYPOINT ["java", "-jar", "app.jar"]