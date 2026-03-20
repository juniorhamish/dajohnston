### Exposing Services

This document explains how our services are exposed to the internet using the managed networking
features of Google Cloud Run and Vercel.

#### 1. Managed HTTPS Endpoints

One of the key benefits of using modern PaaS (Platform as a Service) providers is that they handle
the complexity of SSL/TLS certificates and ingress routing automatically.

* **Frontend (Vercel):** Every deployment to Vercel automatically receives a secure HTTPS URL (e.g.,
  `portal.vercel.app`). When a custom domain is added, Vercel automatically provisions and renews a
  Let's Encrypt SSL certificate.
* **Backend (Google Cloud Run):** Each Cloud Run service is assigned a unique, stable HTTPS
  endpoint (e.g., `https://backend-xyz.a.run.app`). Google Cloud manages the infrastructure, load
  balancing, and SSL termination.

#### 2. Custom Domains

To provide a professional experience, we map our services to custom subdomains (e.g.,
`portal.dajohnston.co.uk` and `api.dajohnston.co.uk`).

* **Vercel Domains:** Configured in the Vercel Dashboard by adding a CNAME record to our DNS
  provider.
* **Cloud Run Domain Mapping:** We use Google Cloud's "Domain Mapping" feature or a Global External
  HTTP(S) Load Balancer to point our custom API domain to the Cloud Run service.

#### 3. Security & Global Load Balancing

Both Vercel and Google Cloud Run provide built-in security features at the edge:

* **DDoS Protection:** Automated mitigation of common network-layer attacks.
* **Global CDN:** Vercel automatically caches and serves static frontend assets from edge locations
  closest to the user.
* **IAM Authentication:** For internal services, we can use Google Cloud IAM to restrict access to
  the backend so that only the frontend (or authorized users) can call specific endpoints.

#### 4. Environment Configuration

The frontend must be configured with the correct backend URL. This is handled via the
`NEXT_PUBLIC_API_URL` environment variable in the Vercel Dashboard.

| Service  | Internal URL | Production URL (Example)          |
|:---------|:-------------|:----------------------------------|
| Frontend | N/A          | `https://portal.dajohnston.co.uk` |
| Backend  | N/A          | `https://api.dajohnston.co.uk`    |
