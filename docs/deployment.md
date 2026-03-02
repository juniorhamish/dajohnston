### Deployment Model

This document outlines the strategy for an automated, scalable, and low-cost deployment.

#### 1. Infrastructure Strategy
The system will be "Self-Managed" to avoid vendor lock-in and keep costs low (leveraging free tiers or low-cost VPS).

*   **Virtual Private Server (VPS):** A single low-cost node (e.g., Hetzner, DigitalOcean, or Oracle Cloud Free Tier) will host the backend and infrastructure.
*   **Next.js (Frontend):** Deployed to **Vercel** for the best performance (CDN) and free-tier availability. This provides an automated "Git-to-Deploy" workflow.

#### 2. Containerization (Docker & K3s)
To meet the requirement for "Advanced Tech" and "Scalability," all core components (excluding the frontend) will run on a **K3s** cluster.

*   **K3s:** A lightweight, CNCF-certified Kubernetes distribution designed for low-resource environments (e.g., 1-2GB RAM VPS).
*   **Docker:** All backend services, the database, and the Identity Service (Keycloak) will be built as Docker images.

#### 3. Automated CI/CD Pipeline
Every code change pushed to the main repository will trigger an automated pipeline using **GitHub Actions**:

1.  **Test:** Run Java (JUnit) and Frontend (Vitest/Playwright) tests.
2.  **Build:** Create Docker images for the modified services.
3.  **Push:** Upload images to a private container registry (e.g., GitHub Container Registry - GHCR).
4.  **Deploy:**
    *   **Frontend:** Automatically handled by Vercel upon code push.
    *   **Infrastructure:** GitHub Actions will update the K3s cluster with the new image tags using `kubectl` or a GitOps tool like Flux/ArgoCD (if we want to go even more "advanced").

#### 4. Scalability
*   **K3s Scaling:** We can easily add more nodes to the K3s cluster if resource usage increases.
*   **Horizontal Pod Autoscaling (HPA):** Kubernetes can automatically scale the number of pods for a given service based on CPU/Memory usage.
*   **Database Scaling:** For initial low-cost, a single PostgreSQL instance will be used. For high availability, a managed DB or a clustered approach (like Patroni) can be used later.

#### 5. Infrastructure Sizing & Resource Allocation
As the system is intended for a small number of users (e.g., a household of 2-5 people), we can optimize resource allocation to fit within a single low-cost VPS node.

| Component | CPU (Requests/Limits) | RAM (Requests/Limits) | Storage | Notes |
| :--- | :--- | :--- | :--- | :--- |
| **K3s (Control Plane)** | 0.1 / 0.2 Core | 256MB / 512MB | 2GB | Core Kubernetes overhead. |
| **Keycloak (Auth)** | 0.2 / 0.5 Core | 512MB / 1GB | 1GB | JVM-based, requires some RAM for start-up. |
| **PostgreSQL (DB)** | 0.1 / 0.5 Core | 256MB / 512MB | 10GB+ | Scales with data; 10GB is plenty for initial apps. |
| **Spring Boot (Backend)** | 0.1 / 0.3 Core | 384MB / 768MB | 500MB | JVM-based; optimized for low-memory (e.g., `-Xmx512m`). |
| **Total (Estimated)** | **0.5 / 1.5 Cores** | **1.4GB / 2.8GB** | **~15GB** | Fits comfortably on a 4GB RAM VPS. |

*   **Recommended VPS Spec:** 2 vCPU, 4GB RAM (e.g., Oracle Cloud ARM "Always Free" or Hetzner CX21).
*   **Optimization:** Using GraalVM Native Image for Spring Boot could further reduce RAM to ~50-100MB per service if needed, though it increases build complexity.

#### 6. Cost Analysis (Initial Development)
| Component | Provider | Cost |
| :--- | :--- | :--- |
| Frontend | Vercel | $0 (Free Tier) |
| Backend & DB | Oracle Cloud / Hetzner | $0 - $5/mo | 2 vCPU, 4GB RAM recommended |
| Auth (Keycloak) | Self-hosted on VPS | $0 (Included in VPS cost) |
| CI/CD | GitHub Actions | $0 (Free Tier for public/private repos) |
| **Total** | | **$0 - $5/month** |

#### 7. Local Development
For instructions on running the entire stack locally for development and testing purposes, please refer to the **[Local Development Guide](local_development.md)**.
