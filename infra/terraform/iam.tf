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

# Workload Identity Pool
resource "google_iam_workload_identity_pool" "github_pool" {
  workload_identity_pool_id = "github-actions-pool"
  display_name              = "GitHub Actions Pool"
  description               = "Identity pool for GitHub Actions"

  depends_on = [google_project_service.iam]
}

# Workload Identity Pool Provider for GitHub
resource "google_iam_workload_identity_pool_provider" "github_provider" {
  workload_identity_pool_id          = google_iam_workload_identity_pool.github_pool.workload_identity_pool_id
  workload_identity_pool_provider_id = "github-actions-provider"
  display_name                       = "GitHub Actions Provider"
  description                        = "GitHub Actions Provider for Workload Identity"

  attribute_mapping = {
    "google.subject"             = "assertion.sub"
    "attribute.actor"            = "assertion.actor"
    "attribute.repository"       = "assertion.repository"
    "attribute.repository_owner" = "assertion.repository_owner"
  }

  # It's highly recommended to use a condition to restrict access to only your repository.
  # The error "The attribute condition must reference one of the provider's claims"
  # often occurs when a condition is missing or incorrectly references claims.
  attribute_condition = "assertion.repository == '${var.github_repository}'"

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}

# Allow GitHub Actions to impersonate the service account
resource "google_service_account_iam_member" "github_wif_member" {
  service_account_id = google_service_account.github_actions.name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github_pool.name}/attribute.repository/${var.github_repository}"
}

# Output the Workload Identity Provider name for use in GitHub Actions
output "workload_identity_provider" {
  value = google_iam_workload_identity_pool_provider.github_provider.name
}
