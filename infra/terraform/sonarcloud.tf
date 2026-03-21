resource "sonarcloud_project" "portal_project" {
  key        = var.sonarcloud_project_key
  name       = "Multi-App Portal"
  visibility = "public"
}

# Example of setting up properties, although specific rules are to be determined.
# For now, just create the basic project.
