terraform {
  required_version = ">= 1.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 7.24.0"
    }
    auth0 = {
      source  = "auth0/auth0"
      version = ">= 1.41.0"
    }
    neon = {
      source  = "kislerdm/neon"
      version = "0.13.0"
    }
  }
}

provider "google" {
  project = var.project_id
  region  = var.region
}

provider "auth0" {
  domain        = var.auth0_domain
  client_id     = var.auth0_client_id
  client_secret = var.auth0_client_secret
}

provider "neon" {
  api_key = var.neon_api_key
}
