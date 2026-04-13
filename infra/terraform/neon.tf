resource "neon_project" "portal_project" {
  name                      = var.neon_project_name
  history_retention_seconds = 21600
}

resource "neon_role" "db_admin" {
  project_id = neon_project.portal_project.id
  branch_id  = neon_project.portal_project.default_branch_id
  name       = var.database_user
}

resource "neon_role" "db_app" {
  project_id = neon_project.portal_project.id
  branch_id  = neon_project.portal_project.default_branch_id
  name       = var.database_app_user
}

import {
  to = neon_role.db_app
  id = "${neon_project.portal_project.id}/${neon_project.portal_project.default_branch_id}/${var.database_app_user}"
}

resource "neon_database" "portal_db" {
  project_id = neon_project.portal_project.id
  branch_id  = neon_project.portal_project.default_branch_id
  name       = var.database_name
  owner_name = neon_role.db_admin.name
}

output "database_url" {
  description = "Connection URL for the application user (subject to RLS)"
  value       = "postgresql://${neon_role.db_app.name}:${neon_role.db_app.password}@${neon_project.portal_project.database_host}/${neon_database.portal_db.name}?sslmode=require"
  sensitive   = true
}

output "flyway_database_url" {
  description = "Connection URL for the admin/migration user (table owner)"
  value       = "postgresql://${neon_role.db_admin.name}:${neon_role.db_admin.password}@${neon_project.portal_project.database_host}/${neon_database.portal_db.name}?sslmode=require"
  sensitive   = true
}
