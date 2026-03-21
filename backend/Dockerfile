# Build stage
FROM amazoncorretto:26 AS build
RUN yum install -y findutils
WORKDIR /app

# Copy gradle files
COPY gradlew .
RUN chmod +x gradlew
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .

# Build only dependencies to cache them in a separate layer
# This helps when only source code changes
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew build -x test --no-daemon

# Run stage
FROM amazoncorretto:26
WORKDIR /app
COPY --from=build /app/build/libs/portal-backend-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
