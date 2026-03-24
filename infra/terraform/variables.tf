variable "project_id" {
  description = "The Google Cloud Project ID"
  type        = string
}

variable "region" {
  description = "The Google Cloud region to deploy to"
  type        = string
  default     = "europe-west1"
}

variable "service_name" {
  description = "The name of the Cloud Run service"
  type        = string
  default     = "portal-backend"
}

variable "repository_name" {
  description = "The name of the Artifact Registry repository"
  type        = string
  default     = "portal-artifacts"
}

variable "auth0_domain" {
  description = "The Auth0 domain"
  type        = string
}

variable "auth0_client_id" {
  description = "The Client ID of the Auth0 Management application"
  type        = string
}

variable "auth0_client_secret" {
  description = "The Client Secret of the Auth0 Management application"
  type        = string
  sensitive   = true
}

variable "auth0_api_identifier" {
  description = "The API identifier for the JWT audience"
  type        = string
  default     = "https://api.dajohnston.co.uk"
}

variable "neon_api_key" {
  description = "The API key for Neon"
  type        = string
  sensitive   = true
}

variable "neon_project_name" {
  description = "The name of the Neon project"
  type        = string
  default     = "portal-db"
}

variable "database_name" {
  description = "The name of the database"
  type        = string
  default     = "portal_db"
}

variable "database_user" {
  description = "The name of the database user"
  type        = string
  default     = "portal_admin"
}

variable "vercel_api_token" {
  description = "The Vercel API Token"
  type        = string
  sensitive   = true
}

variable "vercel_team_id" {
  description = "The Vercel Team ID (optional)"
  type        = string
  default     = null
}

variable "github_repository" {
  description = "The GitHub repository to connect to Vercel (format: 'username/repo')"
  type        = string
}

variable "repo_main_branch" {
  description = "The main branch of the repository"
  type        = string
  default     = "main"
}

variable "billing_account" {
  description = "The ID of the billing account to associate with the budget"
  type        = string
}

variable "monthly_budget_amount" {
  description = "The monthly budget amount in USD"
  type        = number
  default     = 10
}
