# Data source for the project number
data "google_project" "project" {}

resource "google_secret_manager_secret_iam_member" "secret_access" {
  secret_id = "portal-db-password"
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${data.google_project.project.number}-compute@developer.gserviceaccount.com"
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

# Artifact Registry for Backend Docker images
resource "google_artifact_registry_repository" "portal_repo" {
  location      = var.region
  repository_id = var.repository_name
  description   = "Docker repository for Portal backend"
  format        = "DOCKER"

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
        limits = {
          cpu    = "1"
          memory = "512Mi"
        }
      }

      env {
        name  = "DATABASE_URL"
        value = "jdbc:postgresql://${neon_project.portal_project.database_host}/${neon_database.portal_db.name}?sslmode=require"
      }
      env {
        name  = "DATABASE_USERNAME"
        value = neon_role.db_admin.name
      }
      env {
        name = "DATABASE_PASSWORD"
        value_source {
          secret_key_ref {
            secret  = google_secret_manager_secret.db_password.secret_id
            version = "latest"
          }
        }
      }
    }

    scaling {
      max_instance_count = 10
      min_instance_count = 0
    }
  }

  traffic {
    type    = "TRAFFIC_TARGET_ALLOCATION_TYPE_LATEST"
    percent = 100
  }

  depends_on = [
    google_project_service.cloud_run,
    google_secret_manager_secret_iam_member.db_password_access
  ]
}

# Allow the Cloud Run service account to access the database password secret
resource "google_secret_manager_secret_iam_member" "db_password_access" {
  secret_id = google_secret_manager_secret.db_password.id
  role      = "roles/secretmanager.secretAccessor"
  member    = "serviceAccount:${data.google_project.project.number}-compute@developer.gserviceaccount.com"
}

# Allow public (unauthenticated) access to the service (it's secured via Auth0 in the code)
resource "google_cloud_run_v2_service_iam_member" "public_access" {
  project  = var.project_id
  location = google_cloud_run_v2_service.backend.location
  name     = google_cloud_run_v2_service.backend.name
  role     = "roles/run.invoker"
  member   = "allUsers"
}
