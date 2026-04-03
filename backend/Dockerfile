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
COPY portal/build.gradle portal/
COPY shared-security/build.gradle shared-security/

# Build only dependencies to cache them in a separate layer
# This helps when only source code changes
RUN ./gradlew :portal:dependencies --no-daemon

# Copy source code
COPY portal/src portal/src
COPY shared-security/src shared-security/src

# Build the application
ARG VERSION=0.0.1-SNAPSHOT
RUN ./gradlew :portal:build -x check -Pversion=${VERSION} --no-daemon

# Run stage
FROM amazoncorretto:26
WORKDIR /app
COPY --from=build /app/portal/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
