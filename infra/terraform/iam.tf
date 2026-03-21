# Service account for GitHub Actions
resource "google_service_account" "github_actions" {
  account_id   = "github-actions-deployer"
  display_name = "GitHub Actions Deployment Service Account"
}

# Grant permission to push to Artifact Registry
resource "google_artifact_registry_repository_iam_member" "ar_writer" {
  location   = google_artifact_registry_repository.portal_repo.location
  repository = google_artifact_registry_repository.portal_repo.name
  role       = "roles/artifactregistry.writer"
  member     = "serviceAccount:${google_service_account.github_actions.email}"
}

# Grant permission to deploy to Cloud Run
resource "google_cloud_run_v2_service_iam_member" "run_admin" {
  location = google_cloud_run_v2_service.backend.location
  name     = google_cloud_run_v2_service.backend.name
  role     = "roles/run.admin"
  member   = "serviceAccount:${google_service_account.github_actions.email}"
}

# Grant permission to act as the Cloud Run service account
resource "google_service_account_iam_member" "act_as_compute" {
  service_account_id = "projects/${var.project_id}/serviceAccounts/${data.google_project.project.number}-compute@developer.gserviceaccount.com"
  role               = "roles/iam.serviceAccountUser"
  member             = "serviceAccount:${google_service_account.github_actions.email}"
}

# Output the Service Account email for use in GitHub Actions
output "github_actions_service_account_email" {
  value = google_service_account.github_actions.email
}
