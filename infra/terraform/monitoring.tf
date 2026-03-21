resource "google_monitoring_uptime_check_config" "backend_uptime" {
  display_name = "Backend Liveness Check"
  timeout      = "10s"
  period       = "60s"

  http_check {
    path         = "/actuator/health/liveness"
    port         = 443
    use_ssl      = true
    validate_ssl = true
  }

  monitored_resource {
    type = "uptime_url"
    labels = {
      project_id = var.project_id
      host       = replace(replace(google_cloud_run_v2_service.backend.uri, "https://", ""), "/", "")
    }
  }

  content_matchers {
    content = "UP"
    matcher = "CONTAINS_STRING"
  }

  selected_regions = [
    "USA",
    "EUROPE",
    "ASIA_PACIFIC"
  ]
}

resource "google_storage_bucket" "log_bucket" {
  name                        = "${var.project_id}-logs"
  location                    = var.region
  force_destroy               = true
  uniform_bucket_level_access = true

  lifecycle_rule {
    condition {
      age = 30
    }
    action {
      type = "Delete"
    }
  }

  depends_on = [google_project_service.storage]
}

resource "google_logging_project_sink" "log_sink" {
  name                   = "backend-log-sink"
  description            = "Export backend logs to GCS"
  destination            = "storage.googleapis.com/${google_storage_bucket.log_bucket.name}"
  filter                 = "resource.type=\"cloud_run_revision\" AND resource.labels.service_name=\"${var.service_name}\""
  unique_writer_identity = true
}

resource "google_storage_bucket_iam_member" "log_sink_writer" {
  bucket = google_storage_bucket.log_bucket.name
  role   = "roles/storage.objectCreator"
  member = google_logging_project_sink.log_sink.writer_identity
}

resource "google_billing_budget" "budget" {
  billing_account = var.billing_account
  display_name    = "Monthly Budget Alert"

  budget_filter {
    projects = ["projects/${data.google_project.project.number}"]
  }

  amount {
    specified_amount {
      currency_code = "GBP"
      units         = tostring(floor(var.monthly_budget_amount))
      nanos         = floor((var.monthly_budget_amount % 1) * 1e9)
    }
  }

  threshold_rules {
    threshold_percent = 0.5
    spend_basis       = "CURRENT_SPEND"
  }

  threshold_rules {
    threshold_percent = 0.9
    spend_basis       = "CURRENT_SPEND"
  }

  threshold_rules {
    threshold_percent = 1.0
    spend_basis       = "CURRENT_SPEND"
  }

  threshold_rules {
    threshold_percent = 1.0
    spend_basis       = "FORECASTED_SPEND"
  }

  depends_on = [google_project_service.billing_budgets]
}
