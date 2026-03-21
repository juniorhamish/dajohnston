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
managing production-like infrastructure on your local machine.

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

#### 4. Cloud Infrastructure Setup (Terraform)

For the production-like cloud environment on Google Cloud, we use **Terraform** to automate resource
creation.

1. Navigate to `infra/terraform/`.
2. Follow the instructions in the **[Deployment Guide](deployment.md)** to run `terraform apply`.

#### 5. Testing Signup & Login Flows (Auth0)

Since we are using Auth0 as our identity provider, you will need to configure your local environment
to point to your Auth0 tenant.

##### A. Auth0 Setup

1. Create a free account at [Auth0](https://auth0.com/).
2. Create a **Regular Web Application** (for the frontend) and an **API** (for the backend).
3. Configure the Allowed Callback URLs, Logout URLs, and Web Origins for `http://localhost:3000`:
    - **Allowed Callback URLs:** `http://localhost:3000/auth/callback`
    - **Allowed Logout URLs:** `http://localhost:3000/`
    - **Allowed Web Origins:** `http://localhost:3000`

##### B. Social Connections

Auth0 allows you to easily enable social logins (Google, GitHub, etc.) from the **Authentication**
menu in the Auth0 Dashboard. Refer to the **[Authentication Guide](authentication_configuration.md)
**
for more details.

#### 6. Running the Backend (Spring Boot)

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

#### 7. Running the Frontend (Next.js)

1. Navigate to the frontend directory.
2. Create a `.env.local` file:
   ```env
   NEXT_PUBLIC_API_URL=http://localhost:8080
   AUTH0_SECRET='use [openssl rand -hex 32] to generate'
   AUTH0_DOMAIN='YOUR_AUTH0_DOMAIN'
   AUTH0_CLIENT_ID='YOUR_CLIENT_ID'
   AUTH0_CLIENT_SECRET='YOUR_CLIENT_SECRET'
   ```
3. Install dependencies: `npm install`.
4. Run in development mode: `npm run dev`.
5. Access the portal at `http://localhost:3000`.

#### 8. Local Testing Strategies

For detailed information on the multi-layered testing approach (Unit, Component, Integration, and
E2E), refer to the **[Testing Strategy](testing_strategy.md)**.

#### 9. Code Quality & Linting

To ensure your code meets the project standards before pushing:

* **Backend:** Run `./gradlew spotlessCheck` to verify Java formatting. Run
  `./gradlew spotlessApply` to automatically fix it.
* **Frontend:** Run `npx biome check` to check for linting and formatting errors. Run
  `npx biome check --apply` to automatically fix them.
* **Formatting:** Formatting is handled automatically by Biome on commit via **Husky**.

For a full list of quality standards, see the **[Code Quality Standards](code_quality.md)**.

#### 10. Postman & API Documentation

A Postman collection is provided in the `postman/` directory, managed as YAML files to enable direct
GitHub integration.

##### A. Importing via GitHub Integration (Recommended)

The recommended way to use the API collection is to import it directly from GitHub, which ensures it
stays in sync with the codebase.

1. Open the Postman application and ensure you are in a Workspace.
2. Click **Import** -> **GitHub**.
3. Select your repository.
4. Postman will automatically detect the collection in the `postman/postman/collections/` directory.
5. The collection includes variables that you should configure in the collection settings:
    * `base_url`: Defaults to `http://localhost:8080`.
    * `auth0_domain`: Your Auth0 tenant domain.
    * `client_id`: Your Auth0 Application Client ID.
    * `client_secret`: Your Auth0 Application Client Secret.

##### B. Manual Sync

As we add new endpoints, the YAML files in the repository will be updated. If you are using the
GitHub integration, you can pull the latest changes directly within Postman.

##### C. Swagger UI

When the backend is running, you can access the interactive API documentation (Swagger UI) at:

* [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* The OpenAPI spec is available
  at [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

#### 11. Manually Obtaining a JWT for API Testing

The Postman collection is pre-configured to use **OAuth 2.0** at the collection level. This allows
all protected requests to inherit the same authentication token.

1. In Postman, select the **Multi-App Portal API** collection.
2. Go to the **Authorization** tab.
3. Ensure the **Type** is set to `OAuth 2.0` and **Add auth data to** is set to `Request Headers`.
4. Scroll down to the **Configure New Token** section. The fields are pre-populated with variables:
    - **Token Name:** `DevToken`
    - **Grant Type:** `Authorization Code`
    - **Callback URL:** `http://localhost:3000/auth/callback`
    - **Auth URL:** `https://{{auth0_domain}}/authorize`
    - **Access Token URL:** `https://{{auth0_domain}}/oauth/token`
    - **Client ID:** `{{client_id}}`
    - **Client Secret:** `{{client_secret}}`
    - **Audience:** `https://api.dajohnston.co.uk`
    - **Scope:** `openid profile email`
5. Click **Get New Access Token**.
6. A browser window will open for the Auth0 login. Log in or sign up.
7. Once Postman receives the token, click **Use Token**.
8. Individual requests (like `Get Protected Message`) are configured to **Inherit auth from parent
   **, so they will automatically use this token.
