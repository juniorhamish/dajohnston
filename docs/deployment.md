### Deployment Model

This document outlines the strategy for an automated, scalable, and low-cost deployment using
cloud-managed services.

#### 1. Infrastructure Strategy

The system is designed for **low-cost**, **low-maintenance**, and **high-portability** by leveraging
modern cloud-managed platforms (PaaS/SaaS). This eliminates the need for manual server management (
K8s/VPS) while providing an enterprise-grade developer experience.

* **Frontend (Next.js):** Deployed to **Vercel** for optimal performance (CDN) and an automated "
  Git-to-Deploy" workflow.
* **Backend (Java/Spring Boot):** Deployed as a containerized service on **Google Cloud Run**.
* **Database (PostgreSQL):** A managed serverless instance provided by **Neon**.
* **Identity (Auth0):** A SaaS identity provider for secure user authentication and management.

#### 2. Infrastructure as Code (Terraform)

To ensure a consistent and repeatable deployment, all Google Cloud, Auth0, and Neon resources are
managed using **Terraform**. This allows us to define the desired state of our infrastructure and
automate its creation and management.

* **Location:** `infra/terraform/`
* **Resources Managed:** Cloud Run service, Artifact Registry repository, Secret Manager, Auth0
  API (Resource Server), Auth0 Application (Client), Neon Project, Neon Database, Neon Role, and
  required APIs.
* **Deployment Workflow:**
    1. Install the Terraform CLI.
    2. Navigate to `infra/terraform/`.
    3. Create a `terraform.tfvars` file (see [Required Variables](#required-terraform-variables)
       below).
    4. Run `terraform init`.
    5. Run `terraform apply`.

##### Required Terraform Variables

To run Terraform, you need to provide several platform-specific variables in a `terraform.tfvars`
file:

| Variable              | Platform         | Description              | How to Get It                                                                                                                      |
|:----------------------|:-----------------|:-------------------------|:-----------------------------------------------------------------------------------------------------------------------------------|
| `project_id`          | **Google Cloud** | Your GCP Project ID      | Go to [GCP Console](https://console.cloud.google.com/), the ID is in the **Project info** card on the Dashboard.                   |
| `auth0_domain`        | **Auth0**        | Your Auth0 Tenant Domain | In [Auth0 Dashboard](https://manage.auth0.com/), click your tenant name (top right). Format: `YOUR_TENANT.auth0.com`.              |
| `auth0_client_id`     | **Auth0**        | Management Client ID     | Create a **Machine to Machine** app in Auth0 (see [Auth0 Guide](authentication_configuration.md#1-auth0-configuration-terraform)). |
| `auth0_client_secret` | **Auth0**        | Management Client Secret | Found in the **Settings** tab of your Machine to Machine app in Auth0.                                                             |
| `neon_api_key`        | **Neon**         | Your Neon API Key        | In [Neon Console](https://console.neon.tech/), go to **Settings** -> **API Keys**.                                                 |
| `vercel_api_token`    | **Vercel**       | Your Vercel API Token    | In [Vercel Dashboard](https://vercel.com/dashboard), click profile picture -> **Settings** -> **Tokens** -> **Create**.            |
| `github_repository`   | **GitHub**       | Repository to link       | The full name of your repository on GitHub (e.g., `username/repository-name`).                                                     |

Example `terraform.tfvars`:

```hcl
project_id          = "my-gcp-project-123"
auth0_domain        = "my-tenant.auth0.com"
auth0_client_id     = "abc123managementid"
auth0_client_secret = "very-secret-auth0-key"
neon_api_key        = "neon-api-key-here"
vercel_api_token    = "your-vercel-token"
github_repository   = "username/repository-name"
```

#### 3. Containerization (Docker & Cloud Run)

To ensure consistency between development and production, the backend is packaged as a standard *
*Docker image**.

* **Google Cloud Run:** A fully managed serverless platform that automatically scales containers
  based on traffic.
* **Scale-to-Zero:** To keep costs at $0 during development, Cloud Run automatically shuts down the
  container when not in use.
* **Portability:** Because it uses standard Docker images, the backend can be moved to any other
  cloud provider (AWS App Runner, Azure Container Apps, or even a self-hosted server) with minimal
  changes.

#### 3. Automated CI/CD Pipeline

Every code change pushed to the main repository triggers an automated pipeline using **GitHub
Actions**:

1. **Test:** Runs Java (JUnit) and Frontend (Vitest/Playwright) tests.
2. **Build:**
    * **Backend:** Builds a Docker image for the Spring Boot application.
    * **Frontend:** Automatically handled by Vercel upon code push.
3. **Push:** Uploads the backend Docker image to **Google Artifact Registry** (or GitHub Container
   Registry).
4. **Deploy:**
    * **Backend:** Updates the Google Cloud Run service to use the newly pushed image.
    * **Frontend:** Automatically deployed by Vercel.

#### 4. Configuration & Secrets Management

The application is configured using Spring Boot's externalized configuration.

* **Cloud Run Environment Variables:** Sensitive information (database URLs, Auth0 secrets) is
  injected as environment variables directly in the Cloud Run service configuration.
* **Secret Manager:** For higher security, sensitive secrets (like API keys) are stored in **Google
  Secret Manager** and mounted into the container at runtime.
* **Vercel Environment Variables:** For the frontend, all configuration (API URLs, Auth0
  credentials) is managed through the Vercel Dashboard.

#### 5. Database (Neon Postgres)

We use **Neon** for a serverless PostgreSQL experience.

* **Serverless Scaling:** Neon scales CPU and RAM automatically.
* **Storage:** 500MB is included in the free tier, which is more than enough for initial application
  needs.
* **Connection:** The backend connects using a standard PostgreSQL connection string (
  `DATABASE_URL`).

#### 6. Monitoring & Logging

Instead of managing a separate monitoring stack (like Prometheus/Grafana), we leverage the built-in
observability of our cloud providers.

* **Google Cloud Monitoring & Logging:** Automatically captures logs (stdout/stderr) and performance
  metrics (CPU, RAM, Request Latency) from the backend containers.
* **Vercel Analytics:** Provides insights into frontend performance and user behavior.
* **Alerting:** Basic alerts (e.g., service down or high error rates) are configured within the
  Google Cloud Console to send notifications via email or webhooks.

#### 7. Cost Analysis (Initial Development)

| Component                | Provider         | Cost (Initial) | Notes                                      |
|:-------------------------|:-----------------|:---------------|:-------------------------------------------|
| **Frontend**             | Vercel           | $0             | Free Tier                                  |
| **Backend**              | Google Cloud Run | $0             | First 2M requests/month free; scales to $0 |
| **Database**             | Neon             | $0             | Free Tier (500MB)                          |
| **Identity**             | Auth0            | $0             | Free Tier (up to 7,000 users)              |
| **Monitoring & Logging** | Google Cloud     | $0             | Included in Free Tiers                     |
| **Total**                |                  | **$0/month**   |                                            |

#### 9. GitHub Actions Secrets

To enable automated backend deployment using **Workload Identity Federation** (recommended by
Google),
you must configure the following **Repository Secrets** in your GitHub repository:

| Secret Name               | Description                                                                                                      |
|:--------------------------|:-----------------------------------------------------------------------------------------------------------------|
| `GCP_PROJECT_ID`          | Your Google Cloud Project ID.                                                                                    |
| `GCP_WIF_PROVIDER`        | The full name of the Workload Identity Provider (output by Terraform as `workload_identity_provider`).           |
| `GCP_WIF_SERVICE_ACCOUNT` | The email of the GitHub Actions service account (output by Terraform as `github_actions_service_account_email`). |

To obtain these values:

1. Run `terraform apply` to create the Workload Identity Federation resources and service account.
2. The values will be displayed in the Terraform outputs.
3. Copy `workload_identity_provider` into the `GCP_WIF_PROVIDER` secret in GitHub.
4. Copy `github_actions_service_account_email` into the `GCP_WIF_SERVICE_ACCOUNT` secret in GitHub.

#### 10. Local Development

For instructions on running the entire stack locally (using Docker Compose), please refer to the *
*[Local Development Guide](local_development.md)**.

