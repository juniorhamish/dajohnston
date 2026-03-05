### Authentication & Identity Provider Configuration

This document explains how to configure Auth0 as the Identity Provider (IdP) for the Multi-App
Portal, including how to set up third-party (social) authentication.

#### 1. Auth0 Configuration

Auth0 is a managed identity service. To use it, you must first create an Auth0 tenant.

##### A. Create an API (Resource Server)

1. Navigate to **Applications** -> **APIs** in the Auth0 Dashboard.
2. Click **Create API**.
3. **Name:** `Portal API`
4. **Identifier:** `https://api.dajohnston.co.uk` (or your chosen identifier).
5. **Signing Algorithm:** `RS256`.

This API identifier will be used as the `audience` in your JWT validation.

##### B. Create an Application (Client)

1. Navigate to **Applications** -> **Applications**.
2. Click **Create Application**.
3. **Name:** `Portal Frontend`.
4. **Type:** `Regular Web Application` (for Next.js App Router).
5. In **Settings**, configure:
    * **Allowed Callback URLs:** `http://localhost:3000/api/auth/callback`
    * **Allowed Logout URLs:** `http://localhost:3000/`
    * **Allowed Web Origins:** `http://localhost:3000`

#### 2. Social Connections (IDPs)

Auth0 simplifies social login by providing pre-built connectors.

1. Navigate to **Authentication** -> **Social**.
2. Click **Create Connection**.
3. Choose a provider (e.g., Google, GitHub, Facebook).
4. Follow the Auth0 prompts to provide the Client ID and Client Secret from the provider's developer
   portal.
5. Once created, go to the **Applications** tab of the connection and enable it for your
   `Portal Frontend` application.

#### 3. Environment Variables

Store your Auth0 credentials securely in environment variables.

##### Backend (`application.yml`)

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://YOUR_DOMAIN.auth0.com/
          audience: https://api.dajohnston.co.uk
```

##### Frontend (`.env.local`)

```env
AUTH0_SECRET='LONG_RANDOM_VALUE'
AUTH0_BASE_URL='http://localhost:3000'
AUTH0_ISSUER_BASE_URL='https://YOUR_DOMAIN.auth0.com'
AUTH0_CLIENT_ID='YOUR_CLIENT_ID'
AUTH0_CLIENT_SECRET='YOUR_CLIENT_SECRET'
```

#### 4. Multi-Tenant Considerations

While Auth0 handles the identity, our application logic handles "Households" (Tenants). The Auth0
`sub` (Subject) claim will be mapped to our internal `users` table, which in turn links to
`households`.
