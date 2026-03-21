### Authentication & Identity Provider Configuration

This document explains how to configure Auth0 as the Identity Provider (IdP) for the Multi-App
Portal, including how to set up third-party (social) authentication.

#### 1. Auth0 Configuration (Terraform)

To automate the setup of Auth0, we use the **Auth0 Terraform Provider**. This ensures that our
API and Application configurations are versioned and reproducible.

To use the Auth0 Terraform provider, you must provide a **Client ID** and **Client Secret** of a
"Machine to Machine" application in Auth0 that has the necessary scopes for the Management API.

##### How to Get Auth0 Terraform Variables

1. **Create a Management Application:**
    - Go to [Auth0 Dashboard](https://manage.auth0.com/) -> **Applications** -> **Applications**.
    - Click **Create Application**.
    - **Name:** `Terraform-Management-App` (or similar).
    - **Type:** **Machine to Machine Applications**.
    - Select the **Auth0 Management API**.
2. **Assign Scopes:**
    - In the popup, you must select the scopes required to manage your tenant's resources.
    - **Required Scopes:**
        - `read:clients`, `update:clients`, `create:clients`, `delete:clients`
        - `read:resource_servers`, `update:resource_servers`, `create:resource_servers`,
          `delete:resource_servers`
        - `read:client_keys`, `update:client_keys`, `create:client_keys`, `delete:client_keys`
    - Click **Authorize**.
3. **Retrieve Credentials:**
    - Navigate to the **Settings** tab of your newly created application.
    - Copy the **Domain**, **Client ID**, and **Client Secret**.
    - Add these to your `infra/terraform/terraform.tfvars` file.

For more details on the resources being managed, refer to `infra/terraform/auth0.tf`.

##### A. Manual Configuration (Optional/Historical)

If you prefer to configure Auth0 manually via the dashboard:

###### 1. Create an API (Resource Server)

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

#### 5. User Management & Registration

##### A. Creating Users Manually (Auth0 Dashboard)

If you need to create a test user without using the frontend signup flow:

1. In the [Auth0 Dashboard](https://manage.auth0.com/), go to **User Management** -> **Users**.
2. Click **Create User**.
3. Provide an email and password.
4. Ensure the user is assigned to the `Username-Password-Authentication` connection (default).

##### B. Registration via Universal Login

The system uses Auth0's **New Universal Login**. When an application initiates an authentication
request (e.g., via Postman or the Frontend), the user will be presented with a login page that also
contains a **Sign Up** tab by default.

To manage whether users can self-register:

1. Go to **Authentication** -> **Database**.
2. Click on your database connection (e.g., `Username-Password-Authentication`).
3. Toggle the **Disable Sign Ups** setting as needed.
