### Implementation Plan

This document outlines the step-by-step implementation strategy for the Multi-App Portal system. It
follows a phased approach to build a solid foundation before adding individual sub-applications.

#### Phase 1: Foundation & Monorepo Setup

**Goal:** Establish the project structure and basic CI/CD.

| Task ID | Task Description                         | Success Criteria                                                                               |
|:--------|:-----------------------------------------|:-----------------------------------------------------------------------------------------------|
| **1.1** | Initialize Monorepo (Git, README)        | Folder structure defined for `backend/`, `frontend/`, `infra/`, and `docs/`. [✓]               |
| **1.2** | Configure Backend Baseline (Spring Boot) | Java 25, Gradle, Spring Boot 4.0.3, Spotless + google-java-format, group uk.co.dajohnston. [✓] |
| **1.3** | Configure Frontend Baseline (Next.js)    | Next.js 16+, Biome, Tailwind CSS, shadcn/ui. [✓]                                               |
| **1.4** | Setup GitHub Actions (CI)                | Pipeline runs on PRs: build, lint, and unit tests for both stacks. [✓]                         |
| **1.5** | Setup Husky & lint-staged                | Biome runs automatically on git commit. [✓]                                                    |

#### Phase 2: Core Infrastructure (Local & Production)

**Goal:** Provide the necessary services for authentication and data storage.

| Task ID  | Task Description                                 | Success Criteria                                                                             |
|:---------|:-------------------------------------------------|:---------------------------------------------------------------------------------------------|
| **2.1**  | Docker Compose for Local Dev                     | `docker-compose.yml` with Postgres. [✓]                                                      |
| **2.2**  | Auth0 Configuration (Terraform)                  | Create Auth0 API and Application using Terraform. [✓]                                        |
| **2.2a** | Documentation: Social Login Setup (Auth0)        | Guide for configuring Google, GitHub, and other third-party Social Connections in Auth0. [✓] |
| **2.2b** | Configure Social Connections                     | Auth0 configured for Google, Facebook, and GitHub login. [✓]                                 |
| **2.2c** | Documentation: Production Email Setup            | Guide for production SaaS options included. [✓]                                              |
| **2.3**  | Database Schema & Migrations (Flyway/Liquibase)  | Baseline schema for `users`, `households`, and `apps`. [✓]                                   |
| **2.4**  | Setup Google Cloud Project & Artifact Registry   | Project created, Artifact Registry for Docker images active. [✓]                             |
| **2.4a** | Define Infrastructure as Code (Terraform)        | `infra/terraform/` contains Cloud Run, AR, Auth0, and Neon configuration. [✓]                |
| **2.4b** | Automate Infrastructure Setup                    | `terraform apply` successfully creates the cloud, Auth0, and Neon environment. [✓]           |
| **2.5**  | Configure Neon Database (via Terraform)          | Serverless Postgres instance created and accessible. [✓]                                     |
| **2.6**  | Configure Vercel Deployment                      | Frontend automatically deploys on push to `main`. [✓]                                        |
| **2.7**  | Baseline Backend Auth Configuration              | Spring Security + Auth0 (JWT validation) implemented in the Portal backend. [✓]              |
| **2.8**  | Baseline Frontend Auth Configuration             | Auth0 Next.js SDK integrated into the Portal and login/logout flow verified. [✓]             |
| **2.9**  | Configure Google Cloud Run for Backend           | Cloud Run service defined and integrated with Artifact Registry.                             |
| **2.10** | CI/CD: Automated Backend Deployment              | GitHub Action to build and deploy Docker image to Cloud Run.                                 |
| **2.11** | Configure Cloud Monitoring & Logging             | Uptime checks and log sinks active in the Google Cloud Console.                              |
| **2.12** | Configure Postman Collection & API Documentation | `postman/` directory contains YAML files for direct GitHub sync with Postman. [✓]            |

#### Phase 3: Shared Identity & Security

**Goal:** Implement the "Zero Effort" security layer for all future apps.

| Task ID | Task Description                    | Success Criteria                                                              |
|:--------|:------------------------------------|:------------------------------------------------------------------------------|
| **3.1** | Shared Security Library (Backend)   | Extract authentication/authorization logic into a reusable shared module.     |
| **3.2** | User Profile Integration (Frontend) | Integrate Auth0 user profile into the UI and handle household/tenant mapping. |
| **3.3** | User/Household Management API       | Endpoints to create/join a household and manage profile.                      |
| **3.4** | Multi-Tenancy (RLS) Implementation  | PostgreSQL Row-Level Security policies active for `household_id`.             |

#### Phase 4: The Portal (The Hub)

**Goal:** The central application where users log in and select their sub-app.

| Task ID | Task Description            | Success Criteria                                                           |
|:--------|:----------------------------|:---------------------------------------------------------------------------|
| **4.1** | Portal Dashboard UI         | User can see their profile and a list of available sub-apps.               |
| **4.2** | App Invitation System       | User can invite others to their household via email/link.                  |
| **4.3** | Shared UI Component Library | Reusable navigation, layout, and theme providers.                          |
| **4.4** | Web Push Notification Setup | Backend service to send push tokens; Frontend service worker registration. |

#### Phase 5: First Sub-App (Spice Tracker)

**Goal:** Verify the "Low Effort" promise by implementing the first app.

| Task ID | Task Description                 | Success Criteria                                               |
|:--------|:---------------------------------|:---------------------------------------------------------------|
| **5.1** | Spice Tracker Backend Module     | New Gradle/Maven module using the shared security/DB layer.    |
| **5.2** | Spice Tracker UI (Inside Portal) | Dedicated route `/apps/spice-tracker` with its own theme.      |
| **5.3** | Inventory Management Logic       | CRUD operations for spices, scoped by `household_id`.          |
| **5.4** | Performance & Integration Tests  | k6 script for data fetching; Testcontainers for DB/Auth tests. |

#### Phase 6: Refinement & Advanced Tech

**Goal:** Optimize for scalability and the "Advanced Tech" requirements.

| Task ID | Task Description                | Success Criteria                                            |
|:--------|:--------------------------------|:------------------------------------------------------------|
| **6.1** | GraalVM Native Image (Optional) | Compile Spring Boot to Native Image for faster cold-starts. |
| **6.2** | Cloud Run Autoscaling & Costs   | Monitor scale-to-zero and verify monthly billing limits.    |
| **6.3** | Lighthouse CI Integration       | Automated performance reports for the Frontend.             |
| **6.4** | SonarCloud Integration          | Unified dashboard for static analysis, security, and bugs.  |

---

### Suggested Order of Execution

1. **Foundation (Phase 1):** Start here to get the CI/CD feedback loop working immediately.
2. **Infrastructure (Phase 2):** Get the local environment running so you can start developing
   features.
3. **Identity (Phase 3):** This is the most critical cross-cutting concern. Once this is done, every
   future app is "secure by default."
4. **Portal (Phase 4):** Build the entry point.
5. **Sub-apps (Phase 5):** Rinse and repeat for each new idea.
