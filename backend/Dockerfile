# Build stage
FROM amazoncorretto:26 AS build
RUN yum install -y findutils
WORKDIR /app

# Copy api directory
COPY api api

# Copy gradle files
WORKDIR /app/backend
COPY backend/gradlew .
RUN chmod +x gradlew
COPY backend/gradle gradle
COPY backend/build.gradle .
COPY backend/settings.gradle .
COPY backend/gradle.properties .
COPY backend/portal/build.gradle portal/
COPY backend/shared-security/build.gradle shared-security/
COPY backend/spice-tracker/build.gradle spice-tracker/

# Build only dependencies to cache them in a separate layer
# This helps when only source code changes
RUN ./gradlew :portal:dependencies --no-daemon

# Copy source code
COPY backend/portal/src portal/src
COPY backend/portal/openapi-mapping.yaml portal
COPY backend/shared-security/src shared-security/src
COPY backend/spice-tracker/src spice-tracker/src
COPY backend/spice-tracker/openapi-mapping.yaml spice-tracker

# Build the application
ARG VERSION=0.0.1-SNAPSHOT
RUN ./gradlew :portal:build -x check -Pversion=${VERSION} --no-daemon

# Run stage
FROM amazoncorretto:26
WORKDIR /app
COPY --from=build /app/backend/portal/build/libs/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
