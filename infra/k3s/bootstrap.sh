#!/bin/bash

# Multi-App Portal - K3s Bootstrap Script
# This script installs a lightweight K3s cluster on a VPS.

set -e

# Configuration
# Note: K3s version should be stable and supported.
K3S_VERSION="v1.31.2+k3s1"

# Check if an IP or Hostname is provided for TLS SAN
PUBLIC_IP=$1

if [ -z "$PUBLIC_IP" ]; then
    echo "Usage: ./bootstrap.sh <VPS_PUBLIC_IP_OR_HOSTNAME>"
    echo "Error: No public IP/Hostname provided. Remote kubectl access will not work without it."
    exit 1
fi

echo "--- Installing K3s ($K3S_VERSION) on $PUBLIC_IP ---"

# Install K3s with remote access configured via --tls-san
# We enable Traefik as the default Ingress controller for now.
# We set permissions to 644 for /etc/rancher/k3s/k3s.yaml to allow SCP easily (not recommended for production, but good for initial setup)
curl -sfL https://get.k3s.io | INSTALL_K3S_VERSION=$K3S_VERSION sh -s - \
  --write-kubeconfig-mode 644 \
  --tls-san "$PUBLIC_IP"

echo "--- K3s Installation Complete ---"
echo ""
echo "To access this cluster from your local machine, follow these steps:"
echo "1. Create a local kubeconfig file:"
echo "   mkdir -p ~/.kube"
echo "   scp root@$PUBLIC_IP:/etc/rancher/k3s/k3s.yaml ~/.kube/config-portal"
echo ""
echo "2. Edit the file (~/.kube/config-portal) and replace '127.0.0.1' with '$PUBLIC_IP'"
echo "   (Tip: sed -i '' \"s/127.0.0.1/$PUBLIC_IP/g\" ~/.kube/config-portal on macOS)"
echo ""
echo "3. Export the KUBECONFIG variable and verify the connection:"
echo "   export KUBECONFIG=~/.kube/config-portal"
echo "   kubectl get nodes"
echo ""
echo "Wait a minute for the cluster to finish starting up before testing the connection."
