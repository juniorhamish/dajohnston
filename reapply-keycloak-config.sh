#!/bin/bash

# Script to re-apply Keycloak realm configuration while preserving user accounts.
# This uses the 'import' command with '--override true' which updates the realm
# settings from the JSON file but keeps existing data in the database.

echo "Re-applying Keycloak configuration..."

# Run the import command in a temporary container
docker compose run --rm keycloak import --dir /opt/keycloak/data/import --override true

if [ $? -eq 0 ]; then
  echo "Import successful. Restarting Keycloak service to apply changes..."
  docker compose restart keycloak
  echo "Keycloak configuration re-applied successfully."
else
  echo "Error: Keycloak configuration import failed."
  exit 1
fi
