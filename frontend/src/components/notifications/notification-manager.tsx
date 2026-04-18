"use client";

import { useUser } from "@auth0/nextjs-auth0/client";
import { useCallback, useEffect } from "react";
import {
  getVapidPublicKeyAction,
  registerSubscriptionAction,
} from "@/components/notifications/notifications-actions";

export function NotificationManager() {
  const { user, isLoading } = useUser();
  const subscribeUser = useCallback(
    async (registration: ServiceWorkerRegistration) => {
      try {
        const vapidPublicKey = await getVapidPublicKeyAction();
        if (!vapidPublicKey) return;

        const subscription = await registration.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: urlBase64ToUint8Array(vapidPublicKey),
        });

        const p256dh = subscription.getKey("p256dh");
        const auth = subscription.getKey("auth");

        await registerSubscriptionAction(
          subscription.endpoint,
          p256dh ? arrayBufferToBase64(p256dh) : "",
          auth ? arrayBufferToBase64(auth) : "",
        );
        console.log("User is subscribed to push notifications.");
      } catch (error) {
        console.error("Failed to subscribe the user: ", error);
      }
    },
    [],
  );
  const unregisterServiceWorker = useCallback(async () => {
    try {
      const registration = await navigator.serviceWorker.getRegistration();
      if (registration) {
        const subscription = await registration.pushManager.getSubscription();
        if (subscription) {
          await subscription.unsubscribe();
        }
        await registration.unregister();
        console.log("Service Worker unregistered and push unsubscribed.");
      }
    } catch (error) {
      console.error("Failed to unregister the Service Worker: ", error);
    }
  }, []);

  const registerServiceWorker = useCallback(async () => {
    try {
      const registration = await navigator.serviceWorker.register("/sw.js", {
        scope: "/",
        updateViaCache: "none",
      });
      console.log("Service Worker registered with scope:", registration.scope);

      // Check current permission
      if (Notification.permission === "default") {
        const permission = await Notification.requestPermission();
        if (permission === "granted") {
          subscribeUser(registration).then();
        }
      } else if (Notification.permission === "granted") {
        subscribeUser(registration).then();
      }
    } catch (error) {
      console.error("Service Worker registration failed:", error);
    }
  }, [subscribeUser]);

  useEffect(() => {
    if (isLoading) return;

    if (user) {
      registerServiceWorker().then();
    } else {
      unregisterServiceWorker().then();
    }
  }, [isLoading, user, registerServiceWorker, unregisterServiceWorker]);

  return null;
}

function urlBase64ToUint8Array(base64String: string) {
  const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
  const rawData = window.atob(base64);
  const outputArray = new Uint8Array(rawData.length);
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
}

function arrayBufferToBase64(buffer: ArrayBuffer) {
  return btoa(String.fromCharCode(...new Uint8Array(buffer)))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");
}
