# ==============================
# 1) BUILD STAGE
# ==============================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom & download dependencies
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# Copy source code
COPY src ./src

# Build jar
RUN mvn -B -q clean package -DskipTests


# ==============================
# 2) RUNTIME STAGE
# ==============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built jar
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8082

# Run Spring Boot app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
