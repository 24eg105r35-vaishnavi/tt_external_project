# Stage 1: Build stage with Maven and JDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

# Pre-fetch dependencies to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build executable jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Lightweight runtime stage with JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create directory for certificate uploads with write permissions
RUN mkdir -p /app/uploads && chmod 777 /app/uploads

# Copy packaged jar from builder stage
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
