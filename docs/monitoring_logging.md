### Monitoring & Logging Strategy

This document outlines the strategy for capturing, storing, and visualizing logs and metrics for the
Multi-App Portal system using built-in cloud observability tools.

#### 1. Logging Strategy

Centralized logging allows us to troubleshoot issues across our distributed services from a single
interface.

* **Backend (Google Cloud Run):**
    * **Collection:** All logs sent to `stdout` and `stderr` by the Spring Boot application are
      automatically captured by **Google Cloud Logging**.
    * **Structured Logging:** The backend uses `logstash-logback-encoder` to output logs in **JSON
      format**. This allows Google Cloud Logging to index fields like `user_id`, `household_id`, and
      `trace_id` automatically, making them easily searchable.
* **Frontend (Vercel):**
    * **Server-Side Logs:** Logs from Next.js SSR and API routes are automatically captured by *
      *Vercel Logs**.
    * **Client-Side Logs:** Critical client-side errors are sent to a dedicated logging endpoint or
      a service like **Sentry** (Free Tier).

#### 2. Metrics & Monitoring

We monitor the health and performance of our services using the native monitoring capabilities of
our cloud providers.

* **Google Cloud Monitoring:**
    * **Automated Metrics:** Cloud Run automatically collects metrics such as **CPU Utilization**, *
      *Memory Utilization**, **Request Count**, and **Request Latency**.
    * **Custom Metrics:** Spring Boot Actuator can be configured to export custom business metrics
      to Google Cloud Monitoring if needed.
* **Vercel Analytics:**
    * **Web Vitals:** Monitors the frontend performance (LCP, FID, CLS) to ensure a high-quality
      user experience.

#### 3. Key Performance Indicators (SLIs)

We focus on the "Four Golden Signals" of monitoring:

1. **Latency:** Time it takes to service a request (P50, P95, P99).
2. **Traffic:** Demand placed on the system (Requests Per Second).
3. **Errors:** Rate of requests that fail (5xx errors vs. 2xx/3xx).
4. **Saturation:** How "full" the service is (CPU and Memory usage).

#### 4. Alerting

Basic alerting is configured to ensure we are notified of critical issues without manual dashboard
monitoring.

* **Uptime Checks:** Configured in Google Cloud to verify that the backend API is reachable.
* **Alerting Policies:**
    * **High Error Rate:** Triggers if the percentage of 5xx responses exceeds a threshold (e.g., >
      1% over 5 minutes).
    * **Service Unreachable:** Triggers if the uptime check fails.
* **Notification Channels:** Alerts are sent via **Email**, **Slack/Discord**, or the **Google Cloud
  Mobile App**.

#### 5. Cost Monitoring

To prevent unexpected cloud spend, we implement automated cost monitoring and alerting.

* **Monthly Budget Alert:** A Google Cloud Budget is configured to monitor the total spend for the
  project.
* **Alerting Thresholds:** Notifications are sent to billing admins when actual or forecasted spend
  reaches:
    * **50% of the monthly budget:** Early warning.
    * **90% of the monthly budget:** Critical warning.
    * **100% of the monthly budget:** Budget reached.
    * **100% of forecasted spend:** Warning that current usage patterns will exceed the budget by
      the end of the month.
* **Autoscaling:** Cloud Run is configured to scale to zero (min instances = 0) to minimize idle
  costs, though uptime checks may keep a single instance active for health monitoring.

#### 6. Local Development Logging

During local development (Docker Compose), logs are accessible via standard terminal commands:

* `docker compose logs -f`
* Spring Boot logs are also visible in the IDE console.

