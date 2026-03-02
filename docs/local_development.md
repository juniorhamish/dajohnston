### Local Development Guide

This document explains how to set up and run the Multi-App Portal system locally for development and testing.

#### 1. Prerequisites
To run all components locally, ensure you have the following installed:
*   **Docker & Docker Compose:** For running infrastructure (Postgres, Keycloak).
*   **Java 21 (JDK):** For the Spring Boot backend.
*   **Node.js (v20+):** For the Next.js frontend.
*   **IDE:** IntelliJ IDEA (recommended for Java) and VS Code (recommended for Frontend).

#### 2. Running Infrastructure with Docker Compose
To simplify local setup, we use a `docker-compose.yml` file (located in the root or `infra/` directory) to spin up the necessary services.

```yaml
services:
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: portal_db
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"

  keycloak:
    image: quay.io/keycloak/keycloak:24.0
    command: start-dev
    environment:
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin
    ports:
      - "8080:8080"
```

**Steps:**
1.  Run `docker compose up -d`.
2.  Access Keycloak at `http://localhost:8080` to configure realms/clients (or use a pre-configured export).

#### 3. Running the Backend (Spring Boot)
1.  Navigate to the backend module.
2.  Configure `application-local.yml` to point to the local Postgres and Keycloak:
    ```yaml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/portal_db
      security:
        oauth2:
          resourceserver:
            jwt:
              issuer-uri: http://localhost:8080/realms/portal-realm
    ```
3.  Run the application: `./gradlew bootRun` or `./mvnw spring-boot:run`.

#### 4. Running the Frontend (Next.js)
1.  Navigate to the frontend directory.
2.  Create a `.env.local` file:
    ```env
    NEXT_PUBLIC_API_URL=http://localhost:8081
    NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8080
    NEXT_PUBLIC_KEYCLOAK_REALM=portal-realm
    NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=portal-frontend
    ```
3.  Install dependencies: `npm install`.
4.  Run in development mode: `npm run dev`.
5.  Access the portal at `http://localhost:3000`.

#### 5. Local Testing Strategies
For detailed information on the multi-layered testing approach (Unit, Component, Integration, and E2E), refer to the **[Testing Strategy](testing_strategy.md)**.

#### 6. Code Quality & Linting
To ensure your code meets the project standards before pushing:
*   **Backend:** Run `./gradlew checkstyleMain` to verify Java formatting.
*   **Frontend:** Run `npx biome check` to check for linting and formatting errors. Run `npx biome check --apply` to automatically fix them.
*   **Formatting:** Formatting is handled automatically by Biome on commit via **Husky**.

For a full list of quality standards, see the **[Code Quality Standards](code_quality.md)**.

#### 7. Mocking (Optional)
For faster UI development without running the full backend/auth, you can use:
*   **MSW (Mock Service Worker):** To intercept API calls in the frontend.
*   **Spring Security Mocking:** Using `@WithMockUser` for controller tests.
