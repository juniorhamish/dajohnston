### Local Development Guide

This document explains how to set up and run the Multi-App Portal system locally for development and
testing.

#### 1. Prerequisites

To run all components locally, ensure you have the following installed:

* **Docker & Docker Compose:** For running infrastructure (Postgres).
* **Java (JDK):** Java 25 (Latest) for the Spring Boot backend.
* **Node.js (v20+):** For the Next.js frontend.
* **Auth0 Tenant:** A free Auth0 account for identity management.
* **IDE:** IntelliJ IDEA (recommended for Java) and VS Code (recommended for Frontend).

#### 2. Running Infrastructure with Docker Compose

To simplify local setup, we use a `docker-compose.yml` file located in the project root to spin up
the necessary services. This is the recommended way to develop locally as it avoids the complexity
of
managing a local Kubernetes cluster.

```bash
docker compose up -d
```

This will start:

* **PostgreSQL:** Accessible at `localhost:5432` (User: `portal_user`, Password: `portal_password`,
  DB: `portal_db`).

#### 3. Managing Infrastructure & Persistence

The local infrastructure (Postgres) uses Docker volumes to ensure that your database data persists
even when containers are stopped or removed.

* **Persistence:** As long as you don't run `docker compose down -v`, your data will be preserved
  in the `postgres_data` volume.

#### 4. Testing Signup & Login Flows (Auth0)

Since we are using Auth0 as our identity provider, you will need to configure your local environment
to point to your Auth0 tenant.

##### A. Auth0 Setup

1. Create a free account at [Auth0](https://auth0.com/).
2. Create a **Regular Web Application** (for the frontend) and an **API** (for the backend).
3. Configure the Allowed Callback URLs, Logout URLs, and Web Origins for `http://localhost:3000`.

##### B. Social Connections

Auth0 allows you to easily enable social logins (Google, GitHub, etc.) from the **Authentication**
menu in the Auth0 Dashboard. Refer to the **[Authentication Guide](authentication_configuration.md)
**
for more details.

#### 5. Running the Backend (Spring Boot)

1. Navigate to the backend module.
2. Configure `application-local.yml` (optional if using default Docker settings) or set environment
   variables to point to your local Postgres and Auth0 domain. The `application.yml` already
   includes sensible defaults for the local Docker Compose setup.

   ```yaml
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: https://YOUR_AUTH0_DOMAIN/
   ```
3. Run the application: `./gradlew bootRun`.

#### 6. Running the Frontend (Next.js)

1. Navigate to the frontend directory.
2. Create a `.env.local` file:
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:8081
   AUTH0_SECRET='use [openssl rand -hex 32] to generate'
   AUTH0_BASE_URL='http://localhost:3000'
   AUTH0_ISSUER_BASE_URL='https://YOUR_AUTH0_DOMAIN'
   AUTH0_CLIENT_ID='YOUR_CLIENT_ID'
   AUTH0_CLIENT_SECRET='YOUR_CLIENT_SECRET'
   ```
3. Install dependencies: `npm install`.
4. Run in development mode: `npm run dev`.
5. Access the portal at `http://localhost:3000`.

#### 7. Local Testing Strategies

For detailed information on the multi-layered testing approach (Unit, Component, Integration, and
E2E), refer to the **[Testing Strategy](testing_strategy.md)**.

#### 8. Code Quality & Linting

To ensure your code meets the project standards before pushing:

* **Backend:** Run `./gradlew spotlessCheck` to verify Java formatting. Run
  `./gradlew spotlessApply` to automatically fix it.
* **Frontend:** Run `npx biome check` to check for linting and formatting errors. Run
  `npx biome check --apply` to automatically fix them.
* **Formatting:** Formatting is handled automatically by Biome on commit via **Husky**.

For a full list of quality standards, see the **[Code Quality Standards](code_quality.md)**.

#### 9. Mocking (Optional)

For faster UI development without running the full backend/auth, you can use:

* **MSW (Mock Service Worker):** To intercept API calls in the frontend.
* **Spring Security Mocking:** Using `@WithMockUser` for controller tests.
