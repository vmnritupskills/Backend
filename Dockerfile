# -------- BUILD STAGE --------
FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

# Copy only Maven files first (for cache)
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copy source
COPY src src

# Build (Docker cache will avoid re-downloads)
RUN ./mvnw -B clean package -DskipTests

# -------- RUN STAGE --------
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /build/target/lms-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
