"use client";

import { useUser } from "@auth0/nextjs-auth0/client";
import { Bell, BellOff } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import {
  getVapidPublicKeyAction,
  registerSubscriptionAction,
} from "@/components/notifications/notifications-actions";

export function NotificationManager() {
  const { user, isLoading } = useUser();
  const [isSupported, setIsSupported] = useState(false);
  const [isSubscribed, setIsSubscribed] = useState(false);
  const [isBusy, setIsBusy] = useState(false);

  // On mount, only check whether the browser supports push notifications and
  // whether the user is already subscribed. Do NOT request permission here:
  // iOS Safari (installed PWA) only allows `Notification.requestPermission`
  // and `PushManager.subscribe` when triggered by a user gesture.
  useEffect(() => {
    const supported =
      "serviceWorker" in navigator &&
      "PushManager" in globalThis &&
      "Notification" in globalThis;
    setIsSupported(supported);

    if (!supported) return;

    (async () => {
      try {
        const registration = await navigator.serviceWorker.getRegistration();
        const subscription = await registration?.pushManager.getSubscription();
        setIsSubscribed(!!subscription);
      } catch (error) {
        console.error("Failed to read push subscription state: ", error);
      }
    })();
  }, []);

  const subscribe = useCallback(async () => {
    setIsBusy(true);
    try {
      const registration = await navigator.serviceWorker.register("/sw.js", {
        scope: "/",
        updateViaCache: "none",
      });
      console.log("Service Worker registered with scope:", registration.scope);

      // Must be called from a user-gesture on iOS PWAs.
      let permission = Notification.permission;
      if (permission === "default") {
        permission = await Notification.requestPermission();
      }
      if (permission !== "granted") {
        console.warn("Notification permission not granted.");
        return;
      }

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
      setIsSubscribed(true);
      console.log("User is subscribed to push notifications.");
    } catch (error) {
      console.error("Failed to subscribe the user: ", error);
    } finally {
      setIsBusy(false);
    }
  }, []);

  const unsubscribe = useCallback(async () => {
    setIsBusy(true);
    try {
      const registration = await navigator.serviceWorker.getRegistration();
      if (registration) {
        const subscription = await registration.pushManager.getSubscription();
        if (subscription) {
          await subscription.unsubscribe();
        }
      }
      setIsSubscribed(false);
      console.log("User unsubscribed from push notifications.");
    } catch (error) {
      console.error("Failed to unsubscribe the user: ", error);
    } finally {
      setIsBusy(false);
    }
  }, []);

  if (isLoading || !user || !isSupported) return null;

  return (
    <button
      type="button"
      onClick={isSubscribed ? unsubscribe : subscribe}
      disabled={isBusy}
      aria-pressed={isSubscribed}
      aria-label={
        isSubscribed ? "Disable notifications" : "Enable notifications"
      }
      title={isSubscribed ? "Disable notifications" : "Enable notifications"}
      className="inline-flex items-center justify-center rounded-md p-2 hover:bg-accent hover:text-accent-foreground transition-colors disabled:opacity-50"
    >
      {isSubscribed ? (
        <Bell className="h-5 w-5" />
      ) : (
        <BellOff className="h-5 w-5" />
      )}
      <span className="sr-only">
        {isSubscribed ? "Disable notifications" : "Enable notifications"}
      </span>
    </button>
  );
}

function urlBase64ToUint8Array(base64String: string) {
  const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding)
    .replaceAll("-", "+")
    .replaceAll("_", "/");
  const rawData = globalThis.atob(base64);
  const outputArray = new Uint8Array(rawData.length);
  for (let i = 0; i < rawData.length; ++i) {
    /* v8 ignore next -- @preserve */
    outputArray[i] = rawData.codePointAt(i) ?? 0;
  }
  return outputArray;
}

function arrayBufferToBase64(buffer: ArrayBuffer) {
  return btoa(String.fromCodePoint(...new Uint8Array(buffer)))
    .replaceAll("+", "-")
    .replaceAll("/", "_")
    .replace(/=+$/, "");
}
