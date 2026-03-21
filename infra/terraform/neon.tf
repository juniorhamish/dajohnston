resource "neon_project" "portal_project" {
  name = var.neon_project_name
}

resource "neon_role" "db_admin" {
  project_id = neon_project.portal_project.id
  branch_id  = neon_project.portal_project.default_branch_id
  name       = var.database_user
}

resource "neon_database" "portal_db" {
  project_id = neon_project.portal_project.id
  branch_id  = neon_project.portal_project.default_branch_id
  name       = var.database_name
  owner_name = neon_role.db_admin.name
}

output "database_url" {
  value     = "postgresql://${neon_role.db_admin.name}:${neon_role.db_admin.password}@${neon_project.portal_project.database_host}/${neon_database.portal_db.name}?sslmode=require"
  sensitive = true
}
