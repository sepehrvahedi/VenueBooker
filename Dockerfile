FROM gradle:8.4.0-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle bootJar --no-daemon

# Use a minimal Java runtime for the final image
FROM amazoncorretto:21

WORKDIR /app

COPY --from=build /app/build/libs/booker.jar .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "booker.jar"]
