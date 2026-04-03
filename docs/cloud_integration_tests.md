### Cloud Integration Testing Plan (API & GCP)

This document outlines the strategy for running integration and end-to-end tests against a live
instance of the Multi-App Portal running in Google Cloud, specifically focusing on validating
API-generated responses.

#### 1. Objectives

- **Verify Real-World Deployment:** Ensure the application (backend and frontend) works correctly
  when deployed to Google Cloud Platform (GCP).
- **Validate API Responses:** Confirm that the backend correctly returns high-quality, structurally
  sound responses for its public API.
- **End-to-End Connectivity:** Test the full integration chain: Frontend -> Backend -> Database (
  Cloud SQL / Neon).
- **PR Isolation:** Ensure that each PR can be tested in an isolated environment without affecting
  the production application or database.

#### 2. Environment Setup

- **GCP Project:** A dedicated `test` or `staging` environment in Google Cloud.
- **Compute:** **Cloud Run** for both backend and frontend services. For each PR, a unique
  revision or a dedicated service instance is deployed (e.g., `backend-pr-123`).
- **Database (Ephemeral):**
    - **Strategy A: Neon Branching (Recommended):** If using Neon, create a new database branch for
      each PR. This provides a full copy of the schema (and optionally data) in seconds.
    - **Strategy B: Schema-per-PR (Cloud SQL):** Create a new PostgreSQL schema or a dedicated
      database instance for the test run.
    - **Strategy C: Multi-Tenant Schema:** Provision a temporary "test tenant" in a shared
      integration database instance.
- **Auth:** A dedicated **Auth0** tenant for tests or a test-only client in the existing tenant.

#### 3. Implementation Details

##### Backend (RestAssured)

The backend integration tests use **RestAssured** for API validation and **JSON Schema Validator**
for structural integrity.

- **Test Suite:** `ApiIntegrationIT.java`
- **Location:** `backend/portal/src/test/java/uk/co/dajohnston/portal/integration/`
- **Execution:**
  ```bash
  cd backend
  ./gradlew integrationTest -Dit.base-url=https://your-ephemeral-url.a.run.app -Dit.auth-token=your-test-token
  ```
- **Validation:** Structural validation is performed using schemas in
  `backend/portal/src/test/resources/schemas/`.

##### Frontend (Playwright)

The frontend E2E tests use **Playwright** to simulate user interactions.

- **Test Suite:** `e2e/home.test.ts`
- **Location:** `frontend/e2e/`
- **Execution:**
  ```bash
  cd frontend
  BASE_URL=https://your-ephemeral-frontend.a.run.app npm run test:e2e
  ```

#### 4. Integration Test Strategy

- **Trigger:** Tests will execute automatically as part of the CI/CD pipeline after a successful
  deployment to the ephemeral test environment.
- **Tools:**
    - **Playwright/Cypress:** To simulate real user interactions in the browser and verify the full
      stack.
    - **RestAssured / k6:** To perform targeted API-level integration tests against the backend's
      public endpoints.
- **Authentication:** Use a pre-configured test user in Auth0. The CI pipeline will store the test
  user's credentials or a long-lived test token as a GitHub Secret.

#### 5. API Response Verification

- **Structural Validation:** Assert that the API returns valid JSON that matches the expected schema
  (e.g., using JSON Schema validation).
- **Data Integrity:** Verify that database operations (CRUD) correctly persist and retrieve data in
  the isolated environment.
- **Error Handling:** Test negative scenarios (e.g., 401 Unauthorized, 404 Not Found, 422
  Validation Error) to ensure the API behaves predictably.
- **Performance Benchmarking:** Track and alert on latency regressions for critical API endpoints.
