# Build stage
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy gradle files
COPY gradlew .
RUN chmod +x gradlew
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test

# Run stage
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/build/libs/portal-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
