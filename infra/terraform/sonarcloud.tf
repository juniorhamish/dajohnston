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

resource "sonarcloud_quality_gate" "portal_gate" {
  name = "Portal Quality Gate"
  conditions = [
    // Less than 100% coverage
    {
      metric = "coverage"
      error  = 100
      op     = "LT"
    },
    // Less than 100% coverage on new code
    {
      metric = "new_coverage"
      error  = 100
      op     = "LT"
    },
    // No new bugs
    {
      metric = "new_bugs"
      error  = 0
      op     = "GT"
    },
    // No new vulnerabilities
    {
      metric = "new_vulnerabilities"
      error  = 0
      op     = "GT"
    },
    {
      error  = "1"
      metric = "new_reliability_rating"
      op     = "GT"
    },
    {
      error  = "1"
      metric = "new_security_rating"
      op     = "GT"
    },
    {
      error  = "1"
      metric = "new_maintainability_rating"
      op     = "GT"
    },
    {
      error  = "100"
      metric = "new_security_hotspots_reviewed"
      op     = "LT"
    },
    {
      error  = "4"
      metric = "new_duplicated_lines_density"
      op     = "GT"
    }
  ]
}

resource "sonarcloud_quality_gate_selection" "portal_gate_selection" {
  gate_id      = sonarcloud_quality_gate.portal_gate.id
  project_keys = [sonarcloud_project.portal_project.key]
}
