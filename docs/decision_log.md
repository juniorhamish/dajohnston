### Decision Log

This document tracks major architectural and technology decisions for the Multi-App Portal project.

| Date | Decision | Rationale | Status |
| :--- | :--- | :--- | :--- |
| 2026-03-01 | **Architecture: Monorepo** | Simplifies shared code (Auth, UI, Types), unified CI/CD, and consistent development experience. | Accepted |
| 2026-03-01 | **Frontend: Next.js + Tailwind CSS** | React-based, excellent developer experience (Vercel support), built-in routing for "Portal" and "Sub-apps", and rapid UI development with Tailwind/shadcn. | Accepted |
| 2026-03-01 | **Backend: Java (Spring Boot)** | Leverages user competence, strong type safety, and a robust ecosystem for microservices/modular monoliths. | Accepted |
| 2026-03-01 | **Database: PostgreSQL (Row-Level Security)** | Powerful, free, and standard for relational data. RLS (Row-Level Security) can simplify multi-tenancy/household logic. | Accepted |
| 2026-03-01 | **Auth: Keycloak (Self-Hosted)** | Open-source, avoids vendor lock-in (Auth0/Clerk), and supports multi-tenancy and SSO across all apps. | Accepted |
| 2026-03-01 | **Deployment: Docker & K3s (Lightweight Kubernetes)** | Satisfies "Advanced Tech" goal, "Self-Managed" preference, and provides scalability with low overhead. | Accepted |
| 2026-03-01 | **Tenancy Model: Household-Based** | Shared "Household" context allows data sharing across specific apps while maintaining private user data. | Accepted |
| 2026-03-01 | **Notifications: Web Push API** | Low cost, works on Web/Android (and modern iOS Safari), avoiding early native app complexity. | Accepted |
| 2026-03-02 | **Local Dev: Docker Compose** | Simple, lightweight way to run Postgres and Keycloak locally for developer testing without K3s complexity. | Accepted |
| 2026-03-02 | **Sizing: 2 vCPU, 4GB RAM Node** | Fits K3s, Keycloak (JVM), Spring Boot (JVM), and Postgres for a small user group (2-5 users) while staying within low-cost/free VPS limits. | Accepted |
| 2026-03-02 | **Testing Strategy: Multi-layered Pyramid** | Ensures high confidence and reliability by combining Unit, Component (RTL), Integration (Testcontainers), and E2E (Playwright) tests. | Accepted |
| 2026-03-02 | **Code Quality: Automated Enforcement** | Adopts automated linting (ESLint/Checkstyle), code coverage (JaCoCo/Vitest, target 80%), and GitHub Quality Gates to maintain high standards with low effort. | Accepted |
| 2026-03-02 | **Frontend Tooling: Biome** | Replaces ESLint and Prettier with Biome. Rationale: Extreme speed, single-tool simplicity (low maintenance), and unified configuration for linting/formatting. | Accepted |
| 2026-03-02 | **Performance Testing Strategy: k6 & Lighthouse** | Selected k6 for backend/API performance testing (developer-friendly, JS-based) and Lighthouse/LCP for frontend metrics to ensure high responsiveness. | Accepted |
| Logging & Monitoring | **Grafana, Loki, Prometheus** | Centralized log aggregation and metrics monitoring for K3s. | Accepted |
| 2026-03-02 | **Implementation Strategy: Phased Rollout** | Divided the project into six phases: Foundation, Infrastructure, Identity, Portal, Sub-apps, and Refinement. This ensures that critical cross-cutting concerns like Auth and Tenantization are built once and reused. | Accepted |
| 2026-03-02 | **Backend Code Quality: Spotless + Google Java Format** | Switched from Checkstyle to Spotless with `google-java-format`. Rationale: Automatic formatting ("apply" capability) reduces developer friction compared to pure linting. | Accepted |
