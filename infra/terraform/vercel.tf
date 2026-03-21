resource "vercel_project" "portal_frontend" {
  name           = "dajohnston-portal"
  framework      = "nextjs"
  root_directory = "frontend"

  git_repository = {
    type              = "github"
    repo              = var.github_repository
    production_branch = "main"
  }
}

output "vercel_project_url" {
  value = "https://${vercel_project.portal_frontend.name}.vercel.app"
}
