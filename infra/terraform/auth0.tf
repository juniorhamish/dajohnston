# Auth0 API (Resource Server)
resource "auth0_resource_server" "portal_api" {
  name        = "Portal API"
  identifier  = "https://api.dajohnston.co.uk"
  signing_alg = "RS256"

  allow_offline_access                            = true
  token_lifetime                                  = 86400
  token_lifetime_for_web                          = 7200
  skip_consent_for_verifiable_first_party_clients = true
}

# Auth0 Application (Client)
resource "auth0_client" "portal_frontend" {
  name        = "Portal Frontend"
  description = "Frontend application for the Multi-App Portal"
  app_type    = "regular_web"
  logo_uri    = "https://www.dajohnston.co.uk/assets/logo-round-B3HmkIwu.png"

  callbacks = [
    "http://localhost:3000/auth/callback",
    "https://portal.dajohnston.co.uk/auth/callback",
    "https://dajohnston-portal.vercel.app/auth/callback"
  ]

  allowed_logout_urls = [
    "http://localhost:3000/",
    "https://portal.dajohnston.co.uk/",
    "https://dajohnston-portal.vercel.app/"
  ]

  web_origins = [
    "http://localhost:3000",
    "https://portal.dajohnston.co.uk",
    "https://dajohnston-portal.vercel.app/"
  ]

  oidc_conformant = true

  jwt_configuration {
    alg = "RS256"
  }
}
