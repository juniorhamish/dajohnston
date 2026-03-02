### System Architecture

This document describes the high-level architecture for the Multi-App Portal system.

#### 1. Overview
The system is designed as a **Modular Monolith** (or a collection of micro-services sharing a common backbone) within a **Monorepo**. It provides a central **Portal** to access multiple independent applications, all sharing a single Authentication and User Management system.

#### 2. Key Components

*   **App Portal (Frontend):** A single-entry web application built with Next.js that serves as a dashboard. It manages the user's session and provides links/routes to specific sub-applications (Spice Tracker, Checklistz, etc.).
*   **Sub-Applications:** Independent feature modules. They can be separate pages within the same Next.js project (for simplicity) or separate apps that share the same UI component library.
*   **Shared Services (Backend):**
    *   **Identity Service (Keycloak):** Handles user registration, login, SSO, and OAuth2/OIDC.
    *   **Core Backend (Java/Spring Boot):** Manages shared entities like Households, Users, Invitations, and Notifications.
    *   **App Services:** Specific logic for each sub-app, ideally organized as independent modules within the Spring Boot project to keep the "level of effort" low.
*   **Messaging (Optional/Future):** A messaging layer (e.g., Redis or a lightweight RabbitMQ/Kafka) for asynchronous tasks like sending push notifications.

#### 3. Data & Tenantization Model
The system uses a **Household-based Multi-tenancy** model:
*   **Users** can belong to one or more **Households**.
*   Each **Household** acts as a tenant for data sharing.
*   **Private Data:** Data associated only with a `UserID`.
*   **Shared Data:** Data associated with a `HouseholdID`.
*   **Row-Level Security (RLS):** In the PostgreSQL database, RLS policies will ensure users only see data belonging to their user ID or a household they are a member of.

#### 4. Shared Concerns
To keep the level of effort low when adding new apps:
*   **Shared UI Kit:** A central directory of components (buttons, cards, forms) using Tailwind CSS and shadcn/ui.
*   **Shared Auth Client:** A library/utility to easily secure new backend endpoints and frontend routes.
*   **Portal Integration:** A standardized way for new apps to register their icon/name in the main Portal.

#### 5. Cross-Platform Strategy
*   Initially, the apps will be **Progressive Web Apps (PWA)**, allowing them to be "installed" on an iPhone home screen and providing access to Web Push notifications.
*   The technology stack (React) allows for a future move to React Native or Capacitor for a true native app experience without a complete rewrite.
