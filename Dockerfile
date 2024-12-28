FROM gradle:8.4.0-jdk21 AS build

WORKDIR /app

# Copy gradle configuration files first
COPY settings.gradle.kts build.gradle.kts ./

# Copy the gradle wrapper files
COPY gradlew* ./
COPY gradle gradle

# Download dependencies first (this will be cached if no changes to build.gradle.kts)
RUN gradle dependencies --no-daemon

# Then copy the source code
COPY src src

# Build the application
RUN gradle bootJar --no-daemon

# Use a minimal Java runtime for the final image
FROM amazoncorretto:21

WORKDIR /app

COPY --from=build /app/build/libs/booker.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "booker.jar"]
