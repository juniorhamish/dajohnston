### Technology Choices

The technology stack has been selected to leverage existing skills (Java, React) while satisfying requirements for low cost, scalability, and ease of use.

#### 1. Frontend: React/Next.js
*   **Next.js:** Provides server-side rendering (SSR), static site generation (SSG), and API routes. Excellent integration with Vercel and great developer experience.
*   **Tailwind CSS:** Utility-first CSS framework for rapid UI development.
*   **shadcn/ui:** A collection of re-usable components built with Tailwind and Radix UI. It provides a "good UI" out-of-the-box with full accessibility.
*   **Lucide React:** Icons for a consistent look.

#### 2. Backend: Java/Spring Boot
*   **Spring Boot:** Used for all backend logic. Its modular nature (Spring Data, Security, Web) makes it ideal for building robust, scalable services.
*   **Spring Security + OAuth2 Resource Server:** Integrates seamlessly with the Identity Service (Keycloak).
*   **Hibernate/JPA:** For easy database interactions.

#### 3. Identity & Access: Keycloak (Self-Hosted)
*   Provides a full-featured Identity Provider (IdP) supporting OIDC/OAuth2.
*   Supports user registration, login, and managing "Groups" or "Realms" which can be mapped to our Household concept.
*   Avoids vendor lock-in to services like Auth0 or Clerk.

#### 4. Database: PostgreSQL
*   Robust, open-source relational database.
*   **Row-Level Security (RLS):** Policies to enforce that users only see data they are authorized to access (Private vs. Household-shared).

#### 5. Notification Engine: Web Push API
*   Allows the backend (Spring Boot) to send push notifications directly to the user's browser or mobile device (when using PWA on iOS/Android).
*   Low-to-zero cost compared to specialized mobile notification services.

#### 6. Advanced Tech & Learning
*   **Docker:** All components (Frontend, Backend, DB, Keycloak) will be containerized.
*   **K3s (Kubernetes):** A lightweight distribution of Kubernetes, perfect for self-hosting on a single, low-cost VPS.
*   **GitHub Actions:** For automated building of Docker images and deployment.

#### 7. Monitoring & Logging
*   **Grafana Loki & Promtail:** Efficient, metadata-indexed log aggregation that fits within small resource limits.
*   **Prometheus & Grafana:** Industry-standard metrics collection and visualization for K3s and Spring Boot.
*   **Structured JSON Logging:** Ensures logs are easily searchable and analyzable across multiple apps.
