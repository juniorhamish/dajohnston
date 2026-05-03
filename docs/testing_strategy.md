### Testing Strategy

This document outlines the multi-layered testing approach for the Multi-App Portal system, ensuring
reliability, scalability, and ease of adding new applications.

#### 1. Testing Pyramid Overview

We follow a standard testing pyramid to balance execution speed, isolation, and confidence.

- **Unit Tests:** High volume, fast execution, focus on business logic.
- **Component Tests:** Moderate volume, focus on UI components and interaction.
- **Integration Tests:** Lower volume, focus on the interaction between services and
  infrastructure (DB, Auth).
- **End-to-End (E2E) Tests:** Smallest volume, focus on critical user flows across the entire stack.

#### 2. Unit Testing

**Focus:** Individual methods, utility functions, and domain logic.

- **Backend (Spring Boot):**
    - **Tools:** JUnit 5, Mockito, AssertJ.
    - **Scope:** Service-layer logic, entity validation, mapper functions.
    - **Execution:** Run `./gradlew test` (or `./mvnw test`).
- **Frontend (Next.js):**
    - **Tools:** Vitest.
    - **Scope:** Utility functions, state management logic, pure React hooks.
    - **Execution:** Run `npm test`.

#### 3. Component Testing (Frontend)

**Focus:** Ensuring UI components render correctly and handle user interactions as expected.

- **Tools:** React Testing Library (RTL).
- **Scope:** Reusable components (from `shadcn/ui`), form validation, and complex UI logic in
  sub-apps (e.g., Spice Tracker dashboard).
- **Strategy:** Verify accessibility (ARIA roles) and user-centric interactions (clicks, input).
  Prefer `userEvent` over `fireEvent` for more realistic event simulation.

#### 4. Integration Testing

**Focus:** Validating that the code works correctly with the real environment (Database, Auth, and
External APIs).

- **Backend (Spring Boot):**
    - **Tools:** Spring Boot Test, **Testcontainers**.
    - **Scope:** Repository layer (PostgreSQL), Security layer (Auth0/JWT validation), and API
      endpoints.
    - **Testcontainers:** Automatically spins up a real PostgreSQL instance in Docker during the
      test phase to avoid using mocks for critical infrastructure.
- **Frontend (Next.js):**
    - **Tools:** MSW (Mock Service Worker).
    - **Scope:** Validating that the frontend correctly handles API responses (success, error,
      loading states) without needing a running backend.

#### 5. End-to-End (E2E) Testing

**Focus:** Critical "happy path" and "negative path" user journeys across the Portal and Sub-apps.

- **Tools:** Playwright (recommended) or Cypress.
- **Scope:**
    - **Authentication:** Login/Logout flow via Auth0.
    - **Tenantization:** Verifying that User A cannot see User B's private data, but can see shared
      Household data.
    - **Cross-App Flows:** Navigating from the Portal to Spice Tracker and back.
    - **Notifications:** Verifying Web Push triggers for specific events.
- **Execution:** Runs against a staging or fully-running local environment.

#### 6. Performance Testing

**Focus:** Ensuring the system remains responsive and stable under expected load, and identifying
performance regressions.

- **Backend (API) Performance:**
    - **Tools:** **k6** (highly recommended for its developer-friendly JS/TS scripts).
    - **Scope:** Stress testing critical endpoints (e.g., Auth validation, multi-tenant data
      fetching).
    - **Execution:** Run as part of the CI/CD pipeline for specific "performance-critical" PRs or on
      a weekly schedule.
- **Frontend Performance (Web Vitals):**
    - **Tools:** **Lighthouse CI** or **Next.js Analytics**.
    - **Scope:** Measuring Core Web Vitals (LCP, FID, CLS) to ensure a high-quality user experience.
    - **Strategy:** Automated checks in the Vercel deployment pipeline to prevent performance
      regressions in the UI.

#### 7. Continuous Integration (CI) & Quality Gates

All tests and code quality checks are automated within the **GitHub Actions** pipeline.

- Every Pull Request must pass Unit and Integration tests.
- **Code Quality Gates:** Linting (Biome, Spotless) and Code Coverage (JaCoCo, Vitest) are
  enforced via **SonarCloud**. A minimum of **80% line coverage** is required for new code.
- E2E tests are run before deployment to the production environment.
- For more details on quality standards, refer to the **[Code Quality Standards](code_quality.md)**.

#### 8. Guidelines for New Applications

To maintain low effort when adding new apps:

- Use the shared Testing Base classes in the Backend to quickly set up Integration tests with
  Testcontainers.
- Leverage shared RTL utilities in the Frontend (available in `@/lib/test-utils`) for
  consistent UI testing.
- Add at least one "Happy Path" E2E test for the new sub-app to ensure core functionality is
  integrated with the Portal.
- **Performance:** For new apps with complex data sets, add a basic k6 script to verify the response
  time of the primary data-fetching API.
