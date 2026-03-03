### Authentication & Identity Provider Configuration

This document explains how to configure Keycloak as the Identity Provider (IdP) for the Multi-App
Portal, including how to set up third-party (social) authentication.

#### 1. Keycloak Identity Providers (IDPs)

Keycloak allows users to authenticate using external Identity Providers. This is commonly used for "
Social Login" (Google, Facebook, GitHub, etc.) or for connecting to other OIDC/SAML-compliant
identity services.

To add a third-party authentication service:

1. **Access the Keycloak Admin Console:**
    * Navigate to `http://localhost:8080` (or your production URL).
    * Log in with the admin credentials (default: `admin`/`admin` for local development).
2. **Select the Realm:**
    * Ensure you are in the `portal-realm`.
3. **Navigate to Identity Providers:**
    * In the left-hand sidebar, click on **Identity Providers**.
4. **Add a Provider:**
    * Click on **Add provider** and select the desired social media account (e.g., Google, Facebook,
      GitHub, X (Twitter), etc.).
5. **Configure the Provider:**
    * You will need a **Client ID** and a **Client Secret** from the third-party service's developer
      portal (e.g., Google Cloud Console, Facebook for Developers, GitHub Developer Settings).
    * **Redirect URI:** Keycloak will provide a "Redirect URI" (usually something like
      `http://localhost:8080/realms/portal-realm/broker/google/endpoint`). You must register this
      URI in the third-party service's configuration.
6. **First Broker Login:**
    * By default, Keycloak will show a "First Broker Login" flow where users might need to confirm
      their email or set a password for their local account linked to the social provider. This can
      be customized under the **Authentication** tab in the sidebar.

#### 2. Common Social Provider Setup

##### Google

1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project or select an existing one.
3. Configure the **OAuth consent screen**.
4. Go to **Credentials** -> **Create Credentials** -> **OAuth client ID**.
5. Select **Web application**.
6. Add the Keycloak redirect URI to **Authorized redirect URIs**.
7. Copy the **Client ID** and **Client Secret** into the Keycloak Google IDP configuration.

##### GitHub

1. Go to your GitHub **Settings** -> **Developer settings** -> **OAuth Apps**.
2. Click **New OAuth App**.
3. Register the application with your portal's URL.
4. Add the Keycloak redirect URI to **Authorization callback URL**.
5. Generate a **Client Secret**.
6. Copy the **Client ID** and **Client Secret** into the Keycloak GitHub IDP configuration.

##### Facebook

1. Go to the [Meta for Developers](https://developers.facebook.com/) portal.
2. Create a new app.
3. Add **Facebook Login** to your app.
4. Configure **Settings** -> **Basic** to get your **App ID** and **App Secret**.
5. In **Facebook Login** -> **Settings**, add the Keycloak redirect URI to **Valid OAuth Redirect
   URIs**.
6. Copy the **App ID** and **App Secret** into Keycloak.

#### 3. Automatic Account Linking

To allow users who already have an account (e.g., via email/password) to link their social media
account:

1. Go to **Identity Providers** -> **[Your Provider]**.
2. Ensure the **Trust Email** setting is enabled if the provider verifies emails (like Google).
3. Under the **Authentication** flow for "First Broker Login", Keycloak can be configured to
   automatically link accounts with the same email address.

#### 4. Environment Variables for Production

In a production environment, it is recommended to manage sensitive information like Client Secrets
using environment variables or secrets management tools. While Keycloak's UI is used for initial
configuration, you can also automate this via the Keycloak REST API or by providing a pre-configured
realm JSON file with placeholders (though secrets should still be handled carefully).
