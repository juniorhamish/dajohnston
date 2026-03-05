# Multi-App Portal System

A scalable, low-cost, and secure system designed to host multiple independent applications (e.g.,
Spice Tracker, Checklistz, Housework Tracker) with shared identity and data-sharing capabilities.

## Project Structure

This project is organized as a monorepo:

- **`backend/`**: Java (Spring Boot) modular monolith.
- **`frontend/`**: Next.js (React) portal and sub-apps.
- **`infra/`**: Infrastructure as Code (K3s manifests, Docker Compose, etc.).
- **`docs/`**: System documentation and architectural decision records.

## Project Overview

This system is built as a **Modular Monolith** within a **Monorepo**. It provides a central "Portal"
for user authentication and application selection, while allowing individual sub-apps to remain
independent and easily added with minimal effort.

### Key Features

- **Centralized Identity:** Single Sign-On (SSO) using Auth0.
- **Household-Based Tenantization:** Share data within a household or keep it private using
  PostgreSQL Row-Level Security (RLS).
- **Cross-Platform:** Usable as a Web application or PWA on iOS/Android.
- **Advanced Tech Stack:** Powered by Java (Spring Boot), Next.js, Docker, and K3s.
- **Low Cost:** Designed to run on a $0 - $5/month VPS.

## Documentation Index

Detailed documentation for various aspects of the system can be found in the `docs/` directory:

### Core Architecture & Design

- **[Architecture](docs/architecture.md):** High-level system design, modularity, and tenantization
  model.
- **[Technology Choices](docs/technology_choices.md):** Detailed breakdown of the selected
  frameworks and tools.
- **[Authentication & Identity Provider](docs/authentication_configuration.md):** Detailed guide on
  setting up Auth0 and social (third-party) logins.
- **[Deployment Model](docs/deployment.md):** Infrastructure strategy, containerization, and cost
  analysis.

### Development & Operations

- **[Local Development Guide](docs/local_development.md):** Instructions for setting up and running
  the system on your machine.
- **[Implementation Plan](docs/implementation_plan.md):** Phased roadmap from foundation to
  production.
- **[Monitoring & Logging](docs/monitoring_logging.md):** Strategy for observability using Grafana,
  Loki, and Prometheus.

### Quality & Testing

- **[Testing Strategy](docs/testing_strategy.md):** Multi-layered approach (Unit, Integration, E2E,
  Performance).
- **[Code Quality Standards](docs/code_quality.md):** Automated enforcement of linting, formatting,
  and coverage.

### History & Decisions

- **[Decision Log](docs/decision_log.md):** Chronological record of major architectural and
  technology choices.

## Getting Started

To get started with local development, please refer to the *
*[Local Development Guide](docs/local_development.md)**.
