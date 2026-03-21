resource "sonarcloud_project" "portal_project" {
  key        = var.sonarcloud_project_key
  name       = "Multi-App Portal"
  visibility = "public"
}

resource "sonarcloud_project_main_branch" "portal_main_branch" {
  project_key = sonarcloud_project.portal_project.key
  name        = var.repo_main_branch
}

# Example of setting up properties, although specific rules are to be determined.
# For now, just create the basic project.
