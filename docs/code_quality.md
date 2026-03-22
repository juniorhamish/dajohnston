### Code Quality Standards

This document outlines the strategy for measuring and maintaining high code quality across the
Multi-App Portal project. We aim for automated enforcement to keep the effort low while ensuring the
system remains scalable and maintainable.

#### 1. Static Analysis & Linting

To maintain a consistent coding style and catch potential bugs early, we use the following tools:

* **Java (Backend):**
    * **Spotless + google-java-format:** Enforces Google Java Style formatting and import
      organization.
    * **SpotBugs:** Finds potential bugs (e.g., null pointer dereferences, resource leaks).
    * **Lombok:** Reduces boilerplate code (must be used consistently).
    * **gradle-versions-plugin:** Identifies outdated dependencies. Run
      `./gradlew dependencyUpdates` to check.
* **Next.js (Frontend):**
    * **Biome:** An all-in-one tool for linting, formatting, and organizing imports. It replaces
      ESLint and Prettier with a single, high-performance binary.
    * **TypeScript:** Strict mode is enabled to catch type-related errors at compile time.
    * **npm outdated:** Identifies outdated dependencies in the frontend.

#### 2. Code Coverage

We measure how much of our code is exercised by tests to identify untested paths.

* **Backend (Java):** **JaCoCo** is used to generate coverage reports.
* **Frontend (Next.js):** **Vitest** (or Jest) coverage reports are generated.
* **Target:** We aim for **80% line coverage** for all new code. Critical business logic (e.g.,
  Auth, Tenantization) should aim for **95%+**.

#### 3. Quality Gates (CI/CD)

Code quality is enforced via **GitHub Actions** on every Pull Request (PR). A PR cannot be merged
unless:

1. **Build Passes:** The code compiles and all tests pass.
2. **Linting Passes:** No Biome/Spotless errors are found.
3. **Coverage Threshold:** Total code coverage does not drop below the defined threshold (80%).
4. **Security Scan:** No high-severity vulnerabilities are detected in dependencies (using
   `npm audit` and `dependency-check`).
5. **Latest Dependencies:** Dependency update checks are run, and a report of outdated dependencies
   is generated.

#### 4. SonarCloud Integration

For a high-level overview of technical debt, code smells, and security vulnerabilities, we integrate
with **SonarCloud** (free for public projects on GitHub).

* **Quality Gate:** SonarCloud will provide a "Pass/Fail" status on PRs based on its analysis. A
  custom Quality Gate is defined via Terraform in `infra/terraform/sonarcloud.tf`, enforcing:
    * **Code Coverage:** ≥ 80% (monorepo average).
    * **New Bugs:** 0.
    * **New Vulnerabilities:** 0.
* **Reliability/Security/Maintainability:** We aim for "A" ratings in all categories.
* **Main Branch Configuration:** The main branch for SonarCloud analysis is centralized in Terraform
  using the `repo_main_branch` variable in `infra/terraform/terraform.tfvars`. If you rename your
  main branch, you must update it there and in the GitHub Actions workflows.

#### 5. Local Enforcement (Pre-commit)

To prevent bad code from even reaching the repository:

* **Husky & lint-staged:** Automatically runs `biome check --apply` on changed files before every
  `git commit`.
* **Git Hooks:** Can be configured to run unit tests for the affected module locally.

#### 6. Code Reviews

Despite automation, human review remains critical for:

* **Architectural Alignment:** Ensuring new sub-apps follow the "Modular Monolith" patterns.
* **Readability:** Ensuring code is easy to understand for the next developer.
* **Data Privacy:** Double-checking that tenantization/RLS logic is correctly implemented for new
  features.
