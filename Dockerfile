# ═══════════════════════════════════════════════
# Stage 1: Build with Maven
# ═══════════════════════════════════════════════
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ═══════════════════════════════════════════════
# Stage 2: Run with JRE 17
# ═══════════════════════════════════════════════
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Create non-root user for security
RUN groupadd -r travelvista && \
    useradd -r -g travelvista travelvista

# Create uploads directory
RUN mkdir -p /app/uploads && \
    chown -R travelvista:travelvista /app

# Copy the built JAR
COPY --from=build --chown=travelvista:travelvista /app/target/*.jar app.jar

USER travelvista

# Expose port (Render injects PORT env var)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/health || exit 1

# Start the application
# Render sets the PORT env var automatically
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar app.jar"]
