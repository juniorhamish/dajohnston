### Local Development Guide

This document explains how to set up and run the Multi-App Portal system locally for development and
testing.

#### 1. Prerequisites

To run all components locally, ensure you have the following installed:

* **Docker & Docker Compose:** For running infrastructure (Postgres, Keycloak).
* **Java (JDK):** Java 25 (Latest) for the Spring Boot backend.
* **Node.js (v20+):** For the Next.js frontend.
* **IDE:** IntelliJ IDEA (recommended for Java) and VS Code (recommended for Frontend).

#### 2. Running Infrastructure with Docker Compose

To simplify local setup, we use a `docker-compose.yml` file located in the project root to spin up
the necessary services. The Keycloak instance is pre-configured with the `portal-realm`,
`portal-frontend` client, and `portal-backend` resource server via an automatic import.

```bash
docker compose up -d
```

This will start:

* **PostgreSQL:** Accessible at `localhost:5432` (User: `portal_user`, Password: `portal_password`,
  DB: `portal_db`).
    * **Note:** Keycloak data is stored in a separate schema named `keycloak` within the `portal_db`
      database.
* **Keycloak:** Accessible at `http://localhost:8080` (Admin: `admin`/`admin`).
    * **Realm:** `portal-realm`
    * **Frontend Client:** `portal-frontend` (Public)
    * **Backend Client:** `portal-backend` (Bearer-only)
    * **Test User:** `portal_user` / `password`

#### 3. Managing Infrastructure & Persistence

The local infrastructure (Postgres and Keycloak) uses Docker volumes to ensure that your data (user
accounts, realm configurations, etc.) persists even when containers are stopped or removed.

* **Persistence:** As long as you don't run `docker compose down -v`, your data will be preserved
  in the `postgres_data` and `keycloak_data` volumes.
* **Reapplying Configuration:** If you modify the realm JSON file in `infra/keycloak` and want to
  re-apply those changes to an existing environment without losing user accounts, you can use the
  provided script:

  ```bash
  ./reapply-keycloak-config.sh
  ```

  This script runs the `import` command inside a temporary container and then restarts the Keycloak
  service to pick up any changes that require a fresh session.

  *Note: The `--override true` flag will replace existing realm settings with the contents of the
  JSON file. User accounts in the database that are NOT in the JSON file are typically preserved,
  but it's always recommended to back up important data before forcing an overwrite.*

#### 4. Testing Signup & Login Flows

With the infrastructure running, you can test user authentication in several ways:

##### A. Using the Keycloak Account Console

1. Visit the **Portal Realm Account Console**: `http://localhost:8080/realms/portal-realm/account`.
2. Click **Sign In**.
3. On the login screen, you will see a **Register** link (self-registration is enabled by default in
   the local realm config).
4. Fill out the registration form. Since `registrationEmailAsUsername` is enabled, your email will
   be
   your username.
5. After registering, you can manage your profile, change passwords, and view active sessions at the
   same URL.

##### B. Manual User Creation (Admin Console)

1. Access the **Keycloak Admin Console**: `http://localhost:8080` (Admin: `admin`/`admin`).
2. Select the **portal-realm** from the top-left dropdown.
3. Navigate to **Users** in the sidebar.
4. Click **Add user** to create a user manually.
5. After creation, go to the **Credentials** tab to set a password (disable "Temporary" if you want
   immediate login).

##### C. Testing with Social Logins

If you have configured social providers (see *
*[Authentication Guide](authentication_configuration.md)**
), the login screen will automatically show buttons for Google, GitHub, etc. Clicking these will
trigger the OAuth2 flow and create a linked account in the `portal-realm`.

**Steps:**

1. Run `docker compose up -d`.
2. Access Keycloak at `http://localhost:8080` to verify the `portal-realm` configuration.
3. (Optional) Configure social logins (Google, GitHub, etc.) by following the *
   *[Authentication & Identity Provider Guide](authentication_configuration.md)**.

#### 5. Running the Backend (Spring Boot)

1. Navigate to the backend module.
2. Configure `application-local.yml` to point to the local Postgres and Keycloak:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/portal_db
       username: portal_user
       password: portal_password
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: http://localhost:8080/realms/portal-realm
   ```
3. Run the application: `./gradlew bootRun` or `./mvnw spring-boot:run`.

#### 6. Running the Frontend (Next.js)

1. Navigate to the frontend directory.
2. Create a `.env.local` file:
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:8081
   NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8080
   NEXT_PUBLIC_KEYCLOAK_REALM=portal-realm
   NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=portal-frontend
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
