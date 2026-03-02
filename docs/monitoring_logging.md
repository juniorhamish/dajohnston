### Monitoring & Logging Strategy

This document outlines the strategy for capturing, storing, and visualizing logs and metrics for the Multi-App Portal system. We aim for a "Self-Managed" but "Advanced" approach that fits within our resource constraints (4GB RAM VPS).

#### 1. Logging Strategy

Centralized logging is essential for a distributed system like our K3s-based multi-app portal. It allows us to troubleshoot issues across multiple services from a single interface.

*   **Log Collection:**
    *   **Promtail:** A lightweight agent deployed as a `DaemonSet` on the K3s cluster. It discovers logs from all running pods, attaches metadata (pod name, namespace, app), and ships them to a central store.
    *   **Alternative (Fluent Bit):** If resource usage is a concern, Fluent Bit can be used as an even lighter alternative.
*   **Log Storage & Indexing:**
    *   **Grafana Loki:** A horizontally-scalable, highly-available, multi-tenant log aggregation system inspired by Prometheus. It only indexes metadata, making it extremely memory-efficient and perfect for our low-cost VPS.
*   **Structured Logging:**
    *   **Backend (Spring Boot):** Logs will be output in **JSON format** using `logstash-logback-encoder`. This ensures logs are easily searchable by fields (e.g., `user_id`, `household_id`, `trace_id`).
    *   **Frontend (Next.js):** Logs from the server-side (Vercel/SSR) are automatically captured by Vercel. For client-side logs, critical errors will be sent to a dedicated logging endpoint on the backend or a service like **Sentry (Free Tier)**.

#### 2. Metrics & Monitoring

We need to monitor the health of the K3s cluster and the performance of our applications.

*   **Metrics Collection:**
    *   **Prometheus:** The industry standard for monitoring Kubernetes environments. It scrapes metrics from our applications (via **Spring Boot Actuator**) and the K3s cluster itself.
*   **Visualization:**
    *   **Grafana:** Provides the "Single Pane of Glass" for both logs (Loki) and metrics (Prometheus). We will use pre-built dashboards for K3s health and custom dashboards for application performance.
*   **Key Metrics (SLIs):**
    *   **Availability:** Success rate of API requests (2xx vs. 5xx).
    *   **Latency:** P95/P99 response times for critical endpoints.
    *   **Throughput:** Requests per second (RPS).
    *   **Resource Usage:** CPU/RAM consumption per pod to ensure we stay within VPS limits.

#### 3. Alerting

To ensure we don't have to watch dashboards 24/7, we will configure basic alerting.

*   **Alertmanager:** Part of the Prometheus ecosystem.
*   **Channels:** Notifications for critical failures (e.g., "PostgreSQL is down") will be sent via **Discord**, **Telegram**, or **Email**.
*   **Thresholds:** Alerts will trigger on high error rates, prolonged latency, or pod crash loops.

#### 4. Implementation Details (K3s)

We will use the **kube-prometheus-stack** Helm chart to deploy the core monitoring components (Prometheus, Grafana, Alertmanager) as it is the most efficient way to manage these in Kubernetes.

| Tool | Resource Usage (Est.) | Purpose |
| :--- | :--- | :--- |
| **Grafana** | 100MB RAM | Dashboards & Visualization |
| **Prometheus** | 256MB RAM | Metrics Storage & Scraping |
| **Loki** | 128MB RAM | Log Aggregation |
| **Promtail** | 50MB RAM | Log Shipping |

*Note: Total monitoring overhead is ~500MB RAM, which fits within our 4GB VPS allocation.*

#### 5. Local Development Logging

During local development (Docker Compose), logs are accessible via standard terminal commands:
*   `docker compose logs -f`
*   Spring Boot logs are also visible in the IDE console.

For testing monitoring locally, we can optionally include a small `grafana/loki` stack in the `docker-compose.yml` if needed.
