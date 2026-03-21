output "backend_url" {
  description = "The URL of the backend Cloud Run service"
  value       = google_cloud_run_v2_service.backend.uri
}

output "repository_url" {
  description = "The URL of the Artifact Registry repository"
  value       = "${var.region}-docker.pkg.dev/${var.project_id}/${var.repository_name}"
}

output "sonarcloud_project_key" {
  description = "The key for the SonarCloud project"
  value       = sonarcloud_project.portal_project.key
}
