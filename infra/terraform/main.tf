# Data source for the project number
data "google_project" "project" {}

resource "google_secret_manager_secret_iam_member" "db_password_access" {
  secret_id = google_secret_manager_secret.db_password.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${data.google_project.project.number}-compute@developer.gserviceaccount.com"
}

resource "google_secret_manager_secret_iam_member" "db_app_password_access" {
  secret_id = google_secret_manager_secret.db_app_password.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${data.google_project.project.number}-compute@developer.gserviceaccount.com"
}

resource "google_secret_manager_secret_iam_member" "vapid_public_key_access" {
  secret_id = google_secret_manager_secret.vapid_public_key.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${data.google_project.project.number}-compute@developer.gserviceaccount.com"
}

resource "google_secret_manager_secret_iam_member" "vapid_private_key_access" {
  secret_id = google_secret_manager_secret.vapid_private_key.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${data.google_project.project.number}-compute@developer.gserviceaccount.com"
}

# Allow the Cloud Run service account to read from the Artifact Registry
resource "google_artifact_registry_repository_iam_member" "ar_reader" {
  location   = google_artifact_registry_repository.portal_repo.location
  repository = google_artifact_registry_repository.portal_repo.name
  role       = "roles/artifactregistry.reader"
  member     = "serviceAccount:${data.google_project.project.number}-compute@developer.gserviceaccount.com"
}

# Enable APIs
resource "google_project_service" "cloud_run" {
  service            = "run.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "artifact_registry" {
  service            = "artifactregistry.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "secret_manager" {
  service            = "secretmanager.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "logging" {
  service            = "logging.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "monitoring" {
  service            = "monitoring.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "iam" {
  service            = "iam.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "iam_credentials" {
  service            = "iamcredentials.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "sts" {
  service            = "sts.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "storage" {
  service            = "storage.googleapis.com"
  disable_on_destroy = false
}

resource "google_project_service" "billing_budgets" {
  service            = "billingbudgets.googleapis.com"
  disable_on_destroy = false
}

# Secret Manager for Database Password
resource "google_secret_manager_secret" "db_password" {
  secret_id = "portal-db-password"
  replication {
    auto {}
  }
  depends_on = [google_project_service.secret_manager]
}

resource "google_secret_manager_secret_version" "db_password_version" {
  secret      = google_secret_manager_secret.db_password.id
  secret_data = neon_role.db_admin.password
}

# Secret Manager for Application User Database Password
resource "google_secret_manager_secret" "db_app_password" {
  secret_id = "portal-db-app-password"
  replication {
    auto {}
  }
  depends_on = [google_project_service.secret_manager]
}

resource "google_secret_manager_secret_version" "db_app_password_version" {
  secret      = google_secret_manager_secret.db_app_password.id
  secret_data = neon_role.db_app.password
}

# Secret Manager for Vapid public key
resource "google_secret_manager_secret" "vapid_public_key" {
  secret_id = "vapid_public_key"
  replication {
    auto {}
  }
  depends_on = [google_project_service.secret_manager]
}

resource "google_secret_manager_secret_version" "vapid_public_key_version" {
  secret      = google_secret_manager_secret.vapid_public_key.id
  secret_data = var.vapid_public_key
}

# Secret Manager for Vapid private key
resource "google_secret_manager_secret" "vapid_private_key" {
  secret_id = "vapid_private_key"
  replication {
    auto {}
  }
  depends_on = [google_project_service.secret_manager]
}

resource "google_secret_manager_secret_version" "vapid_private_key_version" {
  secret      = google_secret_manager_secret.vapid_private_key.id
  secret_data = var.vapid_private_key
}

# Artifact Registry for Backend Docker images
resource "google_artifact_registry_repository" "portal_repo" {
  location      = var.region
  repository_id = var.repository_name
  description   = "Docker repository for Portal backend"
  format        = "DOCKER"

  cleanup_policies {
    id     = "keep-latest-3"
    action = "KEEP"
    most_recent_versions {
      keep_count = 3
    }
  }

  cleanup_policies {
    id     = "delete-old-versions"
    action = "DELETE"
    condition {
      tag_state = "ANY"
    }
  }

  depends_on = [google_project_service.artifact_registry]
}

# Cloud Run Service (Backend)
resource "google_cloud_run_v2_service" "backend" {
  name     = var.service_name
  location = var.region
  ingress  = "INGRESS_TRAFFIC_ALL"

  template {
    containers {
      # This is a placeholder image. The actual image will be pushed via CI/CD.
      image = "us-docker.pkg.dev/cloudrun/container/hello"

      ports {
        container_port = 8080
      }

      # Resources configuration for scale-to-zero
      resources {
        startup_cpu_boost = true
        limits = {
          cpu    = "2"
          memory = "1Gi"
        }
      }

      env {
        name  = "DATABASE_URL"
        value = "jdbc:postgresql://${neon_project.portal_project.database_host}/${neon_database.portal_db.name}?sslmode=require"
      }
      env {
        name  = "DATABASE_USERNAME"
        value = neon_role.db_app.name
      }
      env {
        name = "DATABASE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_app_password.secret_id
            version = "latest"
          }
        }
      }
      env {
        name  = "FLYWAY_USER"
        value = neon_role.db_admin.name
      }
      env {
        name = "FLYWAY_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }
      env {
        name  = "AUTH0_ISSUER_URI"
        value = "https://${var.auth0_domain}/"
      }
      env {
        name  = "AUTH0_AUDIENCE"
        value = auth0_resource_server.portal_api.identifier
      }
      env {
        name  = "AUTH0_DOMAIN"
        value = var.auth0_domain
      }
      env {
        name  = "AUTH0_MANAGEMENT_CLIENT_ID"
        value = auth0_client.m2m_app.client_id
      }
      env {
        name  = "AUTH0_MANAGEMENT_CLIENT_SECRET"
        value = auth0_client_credentials.m2m_credentials.client_secret
      }
      env {
        name = "VAPID_PUBLIC_KEY"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.vapid_public_key.secret_id
            version = "latest"
          }
        }
      }
      env {
        name = "VAPID_PRIVATE_KEY"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.vapid_private_key.secret_id
            version = "latest"
          }
        }
      }
      liveness_probe {
        http_get {
          path = "/actuator/health/liveness"
        }
        timeout_seconds   = 5
        period_seconds    = 10
        failure_threshold = 3
      }
      startup_probe {
        http_get {
          path = "/actuator/health/readiness"
        }
        initial_delay_seconds = 10
        timeout_seconds       = 5
        period_seconds        = 10
        failure_threshold     = 30
      }
    }

    scaling {
      max_instance_count = 1
      min_instance_count = 0
    }
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  lifecycle {
    ignore_changes = [
      template[0].containers[0].image,
      template[0].labels,
      client,
      client_version
    ]
  }

  depends_on = [
    google_project_service.cloud_run,
    google_secret_manager_secret_iam_member.db_password_access,
    google_secret_manager_secret_iam_member.db_app_password_access,
    google_artifact_registry_repository_iam_member.ar_reader
  ]
}

# Allow public (unauthenticated) access to the service (it's secured via Auth0 in the code)
resource "google_cloud_run_v2_service_iam_member" "public_access" {
  project  = var.project_id
  location = google_cloud_run_v2_service.backend.location
  name     = google_cloud_run_v2_service.backend.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
