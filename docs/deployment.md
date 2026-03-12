### Deployment Model

This document outlines the strategy for an automated, scalable, and low-cost deployment.

#### 1. Infrastructure Strategy

The system will be "Self-Managed" to avoid vendor lock-in and keep costs low (leveraging free tiers
or low-cost VPS).

* **Virtual Private Server (VPS):** A single low-cost node (e.g., Hetzner, DigitalOcean, or Oracle
  Cloud Free Tier) will host the backend and infrastructure.
* **Next.js (Frontend):** Deployed to **Vercel** for the best performance (CDN) and free-tier
  availability. This provides an automated "Git-to-Deploy" workflow.

#### 2. Containerization (Docker & K3s)

To meet the requirement for "Advanced Tech" and "Scalability," all core components (excluding the
frontend) will run on a **K3s** cluster.

* **K3s:** A lightweight, CNCF-certified Kubernetes distribution designed for low-resource
  environments (e.g., 1-2GB RAM VPS).
* **Docker:** All backend services and the database will be built as Docker images.

#### 3. Automated CI/CD Pipeline

Every code change pushed to the main repository will trigger an automated pipeline using **GitHub
Actions**:

1. **Test:** Run Java (JUnit) and Frontend (Vitest/Playwright) tests.
2. **Build:** Create Docker images for the modified services.
3. **Push:** Upload images to a private container registry (e.g., GitHub Container Registry - GHCR).
4. **Deploy:**
    * **Frontend:** Automatically handled by Vercel upon code push.
    * **Infrastructure:** GitHub Actions will update the K3s cluster with the new image tags using
      `kubectl` or a GitOps tool like Flux/ArgoCD (if we want to go even more "advanced").

#### 4. Configuration & Secrets Management

The application is configured using Spring Boot's externalized configuration. In production,
sensitive
information and environment-specific settings are provided via environment variables.

* **Environment Variables:** The `application.yml` uses placeholders (e.g., `${DATABASE_URL}`) that
  are injected at runtime.
* **Kubernetes Secrets:** In the K3s cluster, sensitive data like database passwords and Auth0
  secrets are stored as **Kubernetes Secrets** and mounted as environment variables into the pods.
* **Vercel Environment Variables:** For the frontend, all configuration (API URLs, Auth0
  credentials)
  is managed through the Vercel Dashboard's environment variable settings.

#### 5. Scalability

* **K3s Scaling:** We can easily add more nodes to the K3s cluster if resource usage increases.
* **Horizontal Pod Autoscaling (HPA):** Kubernetes can automatically scale the number of pods for a
  given service based on CPU/Memory usage.
* **Database Scaling:** For initial low-cost, a single PostgreSQL instance will be used. For high
  availability, a managed DB or a clustered approach (like Patroni) can be used later.

#### 5. Infrastructure Sizing & Resource Allocation

As the system is intended for a small number of users (e.g., a household of 2-5 people), we can
optimize resource allocation to fit within a single low-cost VPS node.

| Component                 | CPU (Requests/Limits) | RAM (Requests/Limits) | Storage   | Notes                                                   |
|:--------------------------|:----------------------|:----------------------|:----------|:--------------------------------------------------------|
| **K3s (Control Plane)**   | 0.1 / 0.2 Core        | 256MB / 512MB         | 2GB       | Core Kubernetes overhead.                               |
| **PostgreSQL (DB)**       | 0.1 / 0.5 Core        | 256MB / 512MB         | 10GB+     | Scales with data; 10GB is plenty for initial apps.      |
| **Spring Boot (Backend)** | 0.1 / 0.3 Core        | 384MB / 768MB         | 500MB     | JVM-based; optimized for low-memory (e.g., `-Xmx512m`). |
| **Total (Estimated)**     | **0.3 / 1.0 Cores**   | **0.9GB / 1.8GB**     | **~13GB** | Fits comfortably on a 2GB-4GB RAM VPS.                  |

* **Recommended VPS Spec:** 2 vCPU, 4GB RAM (e.g., Oracle Cloud ARM "Always Free" or Hetzner CX21).
* **Optimization:** Using GraalVM Native Image for Spring Boot could further reduce RAM to ~50-100MB
  per service if needed, though it increases build complexity.

#### 6. Cost Analysis (Initial Development)

| Component    | Provider               | Cost                                    |
|:-------------|:-----------------------|:----------------------------------------|
| Frontend     | Vercel                 | $0 (Free Tier)                          |
| Backend & DB | Oracle Cloud / Hetzner | $0 - $5/mo                              | 2 vCPU, 4GB RAM recommended |
| Auth (Auth0) | SaaS                   | $0 (Free Tier)                          | |
| CI/CD        | GitHub Actions         | $0 (Free Tier for public/private repos) |
| **Total**    |                        | **$0 - $5/month**                       |

#### 7. Local Development

For instructions on running the entire stack locally for development and testing purposes, please
refer to the **[Local Development Guide](local_development.md)**.

#### 8. K3s Cluster Setup (VPS)

To set up the K3s cluster on a fresh **Linux VPS**, follow these steps:

> **Important:** K3s requires a Linux environment with a process supervisor like `systemd` or
`openrc`. It cannot be installed directly on macOS or Windows (without a VM/WSL2).

1. **Provision a VPS:** Select a provider (e.g., Hetzner, Oracle Cloud) with at least 2 vCPU and 2GB
   RAM. Ensure it is running a standard Linux distribution (e.g., Ubuntu, Debian).
2. **Run the Bootstrap Script:**
   Copy `infra/k3s/bootstrap.sh` to your VPS or run it directly:
   ```bash
   # On the VPS (as root)
   curl -O https://raw.githubusercontent.com/YOUR_USERNAME/YOUR_REPO/main/infra/k3s/bootstrap.sh
   chmod +x bootstrap.sh
   ./bootstrap.sh <VPS_PUBLIC_IP>
   ```
3. **Configure Local Access:**
   After the script completes, it will provide instructions for copying the `kubeconfig` file to
   your local machine and updating the server IP.
4. **Verify Setup:**
   ```bash
   # On your local machine
   kubectl get nodes
   kubectl get pods -A
   ```

For more details on managing the cluster, refer to the [K3s Documentation](https://docs.k3s.io).

#### 9. Alternative: Local Machine with Port Forwarding & DDNS

If you are hosting K3s on a local machine (e.g., a home server, Raspberry Pi, or VM) instead of a
public VPS, you can use **Port Forwarding** and **Dynamic DNS (DDNS)** to make it accessible from
the internet.

**Port Forwarding:**
Configure your router to forward the following ports to the local IP address of your K3s node:

| Port     | Protocol | Service        | Description                           |
|:---------|:---------|:---------------|:--------------------------------------|
| **6443** | TCP      | Kubernetes API | Required for `kubectl` remote access. |
| **80**   | TCP      | HTTP Ingress   | For web traffic (Traefik).            |
| **443**  | TCP      | HTTPS Ingress  | For secure web traffic (Traefik).     |

**Dynamic DNS (DDNS):**
Since most home internet connections have dynamic public IP addresses, you need a DDNS service (
e.g., DuckDNS, No-IP, or your domain provider's DDNS) to map a stable hostname (e.g.,
`home.yourdomain.com`) to your current public IP.

**K3s Bootstrap with DDNS Hostname:**
When running the `bootstrap.sh` script, use your **DDNS hostname** as the argument. This ensures the
K3s API server certificate includes the hostname in its SAN (Subject Alternative Name).

```bash
./bootstrap.sh home.yourdomain.com
```

**Kubeconfig Configuration:**

1. Copy `/etc/rancher/k3s/k3s.yaml` to your local machine (e.g., `~/.kube/config-portal`).
2. Open the file and replace `127.0.0.1` with your DDNS hostname:
   `server: https://home.yourdomain.com:6443`
3. The `certificate-authority-data` provided by K3s will work correctly because the connection is
   direct and not intercepted by a proxy like Cloudflare.

**Note on NAT Loopback:** Some routers do not support "NAT Loopback" (or "Hairpinning"). If your
router doesn't, you may not be able to use the DDNS hostname while connected to your home Wi-Fi. In
that case, you would need to use the node's local IP when at home.

**Verifying Port Forwarding & Connectivity:**

To test if your router's port forwarding (80/443) and DDNS are working correctly, you can deploy a
simple "whoami" service:

1. **Apply the Test Service:**
   Modify `infra/k3s/test-service.yaml` and replace `REPLACEME_DDNS_HOSTNAME` with your actual DDNS
   hostname (e.g., `home.yourdomain.com`). Then run:
   ```bash
   export KUBECONFIG=~/.kube/config-portal
   kubectl apply -f infra/k3s/test-service.yaml
   ```
2. **Check Deployment:**
   ```bash
   kubectl get pods,ingress whoami-test
   ```
3. **Test External Access:**
   Open a web browser or use `curl` from **outside your home network** (e.g., using your phone's
   mobile data):
    * **HTTP (Port 80):** `curl http://home.yourdomain.com`
    * **HTTPS (Port 443):** `curl -k https://home.yourdomain.com`
      (Note: `-k` is needed as we haven't set up valid SSL certificates yet).

   If successful, you should see a response showing request headers and pod information.

   **Why Hostname is Required:**
   The Ingress resource in `test-service.yaml` uses a `host` rule (the DDNS hostname) to route
   traffic. When you access the service via a web browser or `curl http://home.yourdomain.com`, the
   HTTP `Host` header is set to your hostname. Traefik (the K3s Ingress controller) matches this
   header against its rules.

   If you try to connect via the IP address (e.g., `curl http://1.2.3.4`), the `Host` header is set
   to the IP, which doesn't match the rule, resulting in a 404 error.

   **Testing with IP Address (Optional):**
   If you want to test access via the IP address while keeping the hostname rule, you can force the
   `Host` header using `curl`:
   ```bash
   curl -H "Host: home.yourdomain.com" http://<YOUR_PUBLIC_IP>
   ```

   Alternatively, if you want to allow access via any hostname or IP address for testing, you can
   remove the `host:` line from `infra/k3s/test-service.yaml` so it looks like this:
   ```yaml
   spec:
     rules:
     - http:
         paths:
         - path: /
           ...
   ```

4. **Cleanup:**
   Once verified, you can remove the test service:
   ```bash
   kubectl delete -f infra/k3s/test-service.yaml
   ```
