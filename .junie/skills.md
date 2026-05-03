# Junie Skills

This file defines specific "skills" or "workflows" that Junie can perform. To invoke a skill, the
user should provide the name of the skill and the required parameters.

## plan-app

**Purpose**: Design a new sub-application for the Multi-App Portal.

**Input**:

- `app_name`: The name of the application (e.g., "Checklistz").
- `app_description`: A short description of what the application should do.

**Workflow**:

1. **Initial Analysis**: Analyse the requirements and how they fit into the existing portal
   architecture (shared security, multi-tenancy via RLS, household-scoped data).
2. **Clarifying Questions**: Ask the user specific questions to resolve ambiguities regarding:
    - Core features and user stories.
    - Data entities and their relationships.
    - Specific UI requirements or complex flows.
    - External integrations or specific constraints.
3. **Design Documents**: Once the requirements are clear, create a directory `docs/apps/{app_name}/`
   and generate the following files:
    - `design.md`: A comprehensive design document including:
        - **Overview**: Summary of the app.
        - **User Flows**: Description of the UI screens and transitions.
        - **API Specifications**: Outline of the REST endpoints, matching the project's API
          guidelines.
        - **Technical Details**: Database schema, RLS policies, and Flyway/Liquibase migration
          plans.
        - **Implementation Plan**: A detailed, bit-by-bit breakdown of the task into individual
          steps (e.g., Step 1: Base Entities, Step 2: Core Services, etc.) to facilitate
          incremental implementation.
    - `refactor.md` (Optional): If architectural or code refactors are needed in the shared layers
      or existing portal, document them here. These represent higher-level changes that must be
      applied outside the app-specific plan and should be addressed in a separate session as a
      prerequisite.
4. **User Review**: Inform the user that they can now modify the generated markdown files to further
   clarify intentions or plans.

## implement-app

**Purpose**: Implement a new sub-application based on an approved design.

**Input**:

- `plan_file_path`: Path to the `design.md` file created by `plan-app`.

**Workflow**:

1. **Preparation**: Read the `design.md`. Ensure any prerequisite refactors (from a separate
   `refactor.md` or session) have already been applied.
2. **Step-by-Step Implementation**: Follow the bit-by-bit **Implementation Plan** defined in the
   `design.md`. Each step should be completed and verified independently where possible.
    - Typically, steps will cover:
        - **Backend**: Entities, repositories, services, and controllers. Ensure OpenAPI specs are
          updated.
        - **Database**: Necessary database migrations.
        - **Frontend**: UI components and routes in the Next.js application.
3. **Verification**:
    - Ensure 100% test coverage for the new code.
    - Verify the integration with the shared security and multi-tenancy layers.
    - Ensure the application builds and all tests pass.
