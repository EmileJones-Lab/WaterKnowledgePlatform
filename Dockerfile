# Stage 1: Build
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Copy gradle wrapper and related files for dependency caching
COPY gradlew .
COPY gradle gradle
COPY gradle.properties .
COPY settings.gradle.kts .
COPY buildSrc buildSrc

# Ensure gradlew is executable
RUN chmod +x gradlew

# Pre-download dependencies to improve build speed
RUN ./gradlew help --no-daemon

# Copy the entire source code
COPY . .

# Build the specific module: controller-web
RUN ./gradlew :controller:web:bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the built jar from the build stage
# The jar name 'web.jar' is specified in the build configuration
COPY --from=build /app/controller/web/build/libs/web.jar app.jar

# Expose the application port
# Default port is 8080 as defined in application.yml
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
