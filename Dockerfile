# -------------------------------------------------------------
# Stage 1: Build stage with Maven and OpenJDK 21
# -------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy POM file and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package JAR
COPY src ./src
RUN mvn package -DskipTests -B

# -------------------------------------------------------------
# Stage 2: Minimal Runtime stage with Java 21 JRE
# -------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Add non-root system user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR artifact from build stage
COPY --from=builder /build/target/hr_portal-0.0.1-SNAPSHOT.jar app.jar

# Set permissions
RUN chown -R appuser:appgroup /app
USER appuser

# Expose Spring Boot server port
EXPOSE 9090

# Environmental defaults
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Healthcheck probe using wget on /actuator/health
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:9090/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]