import { useUser } from "@auth0/nextjs-auth0/client";
import { render, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getVapidPublicKey, registerSubscription } from "@/generated/sdk.gen";
import { mockPartial } from "@/lib/test-utils";
import { NotificationManager } from "./notification-manager";

vi.mock("@auth0/nextjs-auth0/client", () => ({
  useUser: vi.fn(),
}));

vi.mock("@/generated/sdk.gen", () => ({
  getVapidPublicKey: vi.fn(),
  registerSubscription: vi.fn(),
}));

describe("NotificationManager", () => {
  const mockRegistration = {
    scope: "http://localhost:3000/",
    pushManager: {
      subscribe: vi.fn(),
      getSubscription: vi.fn(),
    },
    unregister: vi.fn(),
  };

  const mockSubscription = {
    endpoint: "https://example.com/endpoint",
    expirationTime: null,
    getKey: vi.fn(),
    unsubscribe: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();

    // Mock navigator.serviceWorker
    Object.defineProperty(globalThis.navigator, "serviceWorker", {
      writable: true,
      value: {
        register: vi.fn(),
        getRegistration: vi.fn(),
      },
    });

    // Mock Notification
    Object.defineProperty(globalThis, "Notification", {
      writable: true,
      value: {
        permission: "default",
        requestPermission: vi.fn(),
      },
    });

    // Mock PushManager
    Object.defineProperty(globalThis, "PushManager", {
      writable: true,
      value: vi.fn(),
    });

    // Mock window.atob and btoa if needed (they should be available in JSDOM)
  });

  it("should do nothing when user is loading", () => {
    mockPartial(useUser).mockReturnValue({ user: null, isLoading: true });
    render(<NotificationManager />);
    expect(navigator.serviceWorker.register).not.toHaveBeenCalled();
  });

  it("should register service worker when user is logged in", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.register).mockResolvedValue(
      mockRegistration,
    );
    vi.mocked(Notification.requestPermission).mockResolvedValue("granted");
    mockPartial(getVapidPublicKey).mockResolvedValue({
      data: {
        publicKey:
          "BEl62v_q_A-p6Le9To6ZwpS9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s",
      },
    });
    vi.mocked(mockRegistration.pushManager.subscribe).mockResolvedValue(
      mockSubscription,
    );
    mockSubscription.getKey.mockReturnValue(new Uint8Array([1, 2, 3]).buffer);

    render(<NotificationManager />);

    await waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalledWith("/sw.js", {
        scope: "/",
        updateViaCache: "none",
      });
    });

    await waitFor(() => {
      expect(Notification.requestPermission).toHaveBeenCalled();
    });

    await waitFor(() => {
      expect(getVapidPublicKey).toHaveBeenCalled();
      expect(mockRegistration.pushManager.subscribe).toHaveBeenCalled();
      expect(registerSubscription).toHaveBeenCalled();
    });
  });

  it("should unregister service worker when user is logged out", async () => {
    mockPartial(useUser).mockReturnValue({ user: null, isLoading: false });
    mockPartial(navigator.serviceWorker.getRegistration).mockResolvedValue(
      mockRegistration,
    );
    vi.mocked(mockRegistration.pushManager.getSubscription).mockResolvedValue(
      mockSubscription,
    );
    vi.mocked(mockSubscription.unsubscribe).mockResolvedValue(true);
    vi.mocked(mockRegistration.unregister).mockResolvedValue(true);

    render(<NotificationManager />);

    await waitFor(() => {
      expect(navigator.serviceWorker.getRegistration).toHaveBeenCalled();
      expect(mockSubscription.unsubscribe).toHaveBeenCalled();
      expect(mockRegistration.unregister).toHaveBeenCalled();
    });
  });

  it("should handle already granted permission", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.register).mockResolvedValue(
      mockRegistration,
    );
    Object.defineProperty(Notification, "permission", {
      value: "granted",
      configurable: true,
    });
    mockPartial(getVapidPublicKey).mockResolvedValue({
      data: { publicKey: "VAPID_KEY" },
    });
    vi.mocked(mockRegistration.pushManager.subscribe).mockResolvedValue(
      mockSubscription,
    );

    render(<NotificationManager />);

    await waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalled();
      expect(Notification.requestPermission).not.toHaveBeenCalled();
      expect(getVapidPublicKey).toHaveBeenCalled();
    });
  });

  it("should handle failure to grant permission", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.register).mockResolvedValue(
      mockRegistration,
    );
    Object.defineProperty(Notification, "permission", {
      value: "default",
      configurable: true,
    });
    vi.mocked(Notification.requestPermission).mockResolvedValue("denied");

    render(<NotificationManager />);

    await waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalled();
      expect(Notification.requestPermission).toHaveBeenCalled();
      expect(getVapidPublicKey).not.toHaveBeenCalled();
    });
  });

  it("should handle denied permission", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.register).mockResolvedValue(
      mockRegistration,
    );
    Object.defineProperty(Notification, "permission", {
      value: "denied",
      configurable: true,
    });

    render(<NotificationManager />);

    await waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalled();
      expect(Notification.requestPermission).not.toHaveBeenCalled();
      expect(getVapidPublicKey).not.toHaveBeenCalled();
    });
  });

  it("should handle failed service worker registration", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    vi.mocked(navigator.serviceWorker.register).mockRejectedValue(
      new Error("Registration failed"),
    );
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    render(<NotificationManager />);

    await waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(
        "Service Worker registration failed:",
        expect.any(Error),
      );
    });
    consoleSpy.mockRestore();
  });

  it("should handle failed service worker unregistration", async () => {
    mockPartial(useUser).mockReturnValue({ user: null, isLoading: false });
    vi.mocked(navigator.serviceWorker.getRegistration).mockRejectedValue(
      new Error("Unregistration failed"),
    );
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    render(<NotificationManager />);

    await waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(
        "Failed to unregister the Service Worker: ",
        expect.any(Error),
      );
    });
    consoleSpy.mockRestore();
  });

  it("should handle no registration during unregister", async () => {
    mockPartial(useUser).mockReturnValue({ user: null, isLoading: false });
    mockPartial(navigator.serviceWorker.getRegistration).mockResolvedValue(
      undefined,
    );

    render(<NotificationManager />);

    await waitFor(() => {
      expect(navigator.serviceWorker.getRegistration).toHaveBeenCalled();
    });
  });

  it("should handle no subscription during unregister", async () => {
    mockPartial(useUser).mockReturnValue({ user: null, isLoading: false });
    mockPartial(navigator.serviceWorker.getRegistration).mockResolvedValue(
      mockRegistration,
    );
    vi.mocked(mockRegistration.pushManager.getSubscription).mockResolvedValue(
      null,
    );
    vi.mocked(mockRegistration.unregister).mockResolvedValue(true);

    render(<NotificationManager />);

    await waitFor(() => {
      expect(mockRegistration.unregister).toHaveBeenCalled();
    });
  });

  it("should return early if VAPID public key is missing", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.register).mockResolvedValue(
      mockRegistration,
    );
    Object.defineProperty(Notification, "permission", {
      value: "granted",
      configurable: true,
    });
    mockPartial(getVapidPublicKey).mockResolvedValue({ data: {} });

    render(<NotificationManager />);

    await waitFor(() => {
      expect(getVapidPublicKey).toHaveBeenCalled();
      expect(mockRegistration.pushManager.subscribe).not.toHaveBeenCalled();
    });
  });

  it("should handle missing keys in subscription", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.register).mockResolvedValue(
      mockRegistration,
    );
    Object.defineProperty(Notification, "permission", {
      value: "granted",
      configurable: true,
    });
    mockPartial(getVapidPublicKey).mockResolvedValue({
      data: {
        publicKey:
          "BEl62v_q_A-p6Le9To6ZwpS9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s9u8s",
      },
    });
    vi.mocked(mockRegistration.pushManager.subscribe).mockResolvedValue(
      mockSubscription,
    );
    mockSubscription.getKey.mockReturnValue(null); // Return null for both keys

    render(<NotificationManager />);

    await waitFor(() => {
      expect(registerSubscription).toHaveBeenCalledWith(
        expect.objectContaining({
          body: expect.objectContaining({
            keys: { p256dh: "", auth: "" },
          }),
        }),
      );
    });
  });
});
