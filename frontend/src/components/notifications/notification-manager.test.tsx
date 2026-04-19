import { useUser } from "@auth0/nextjs-auth0/client";
import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
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
      configurable: true,
      writable: true,
      value: {
        register: vi.fn(),
        getRegistration: vi.fn().mockResolvedValue(undefined),
      },
    });

    // Mock Notification
    Object.defineProperty(globalThis, "Notification", {
      configurable: true,
      writable: true,
      value: {
        permission: "default",
        requestPermission: vi.fn(),
      },
    });

    // Mock PushManager (feature-detection)
    Object.defineProperty(globalThis, "PushManager", {
      configurable: true,
      writable: true,
      value: vi.fn(),
    });

    mockRegistration.pushManager.subscribe.mockReset();
    mockRegistration.pushManager.getSubscription.mockReset();
    mockRegistration.unregister.mockReset();
    mockSubscription.getKey.mockReset();
    mockSubscription.unsubscribe.mockReset();
  });

  it("should render nothing when user is loading", () => {
    mockPartial(useUser).mockReturnValue({ user: null, isLoading: true });
    const { container } = render(<NotificationManager />);
    expect(container).toBeEmptyDOMElement();
    expect(navigator.serviceWorker.register).not.toHaveBeenCalled();
  });

  it("should render nothing when user is logged out", () => {
    mockPartial(useUser).mockReturnValue({ user: null, isLoading: false });
    const { container } = render(<NotificationManager />);
    expect(container).toBeEmptyDOMElement();
    expect(navigator.serviceWorker.register).not.toHaveBeenCalled();
  });

  it("should render nothing when push notifications are not supported", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    // Remove PushManager to simulate unsupported browser
    // @ts-expect-error - deleting for test
    delete globalThis.PushManager;

    const { container } = render(<NotificationManager />);
    await waitFor(() => {
      expect(container).toBeEmptyDOMElement();
    });
  });

  it("should render an enable-notifications button when logged in and not subscribed", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /enable notifications/i,
    });
    expect(button).toBeInTheDocument();
    expect(button).toHaveAttribute("aria-pressed", "false");
    // Nothing should have run automatically
    expect(navigator.serviceWorker.register).not.toHaveBeenCalled();
    expect(Notification.requestPermission).not.toHaveBeenCalled();
    expect(getVapidPublicKey).not.toHaveBeenCalled();
  });

  it("should render a disable-notifications button when already subscribed", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.getRegistration).mockResolvedValue(
      mockRegistration,
    );
    mockRegistration.pushManager.getSubscription.mockResolvedValue(
      mockSubscription,
    );

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /disable notifications/i,
    });
    expect(button).toBeInTheDocument();
    expect(button).toHaveAttribute("aria-pressed", "true");
  });

  it("should subscribe the user when the enable button is clicked", async () => {
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
    mockRegistration.pushManager.subscribe.mockResolvedValue(mockSubscription);
    mockSubscription.getKey.mockReturnValue(new Uint8Array([1, 2, 3]).buffer);

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /enable notifications/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalledWith("/sw.js", {
        scope: "/",
        updateViaCache: "none",
      });
    });
    await waitFor(() => {
      expect(Notification.requestPermission).toHaveBeenCalled();
      expect(getVapidPublicKey).toHaveBeenCalled();
      expect(mockRegistration.pushManager.subscribe).toHaveBeenCalled();
      expect(registerSubscription).toHaveBeenCalled();
    });

    // After successful subscribe, the button should flip to disable
    await screen.findByRole("button", { name: /disable notifications/i });
  });

  it("should not subscribe the user without an explicit click", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });

    render(<NotificationManager />);

    // Wait for any async mount-effects to complete
    await screen.findByRole("button", { name: /enable notifications/i });

    expect(navigator.serviceWorker.register).not.toHaveBeenCalled();
    expect(Notification.requestPermission).not.toHaveBeenCalled();
    expect(getVapidPublicKey).not.toHaveBeenCalled();
    expect(registerSubscription).not.toHaveBeenCalled();
  });

  it("should reuse an already-granted permission without prompting", async () => {
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
    mockRegistration.pushManager.subscribe.mockResolvedValue(mockSubscription);
    mockSubscription.getKey.mockReturnValue(null);

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /enable notifications/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalled();
      expect(Notification.requestPermission).not.toHaveBeenCalled();
      expect(getVapidPublicKey).toHaveBeenCalled();
    });
  });

  it("should handle a denied permission result", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.register).mockResolvedValue(
      mockRegistration,
    );
    vi.mocked(Notification.requestPermission).mockResolvedValue("denied");

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /enable notifications/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalled();
      expect(Notification.requestPermission).toHaveBeenCalled();
      expect(getVapidPublicKey).not.toHaveBeenCalled();
    });

    // Still shows the enable button
    expect(
      screen.getByRole("button", { name: /enable notifications/i }),
    ).toBeInTheDocument();
  });

  it("should skip prompting when permission is already denied", async () => {
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

    const button = await screen.findByRole("button", {
      name: /enable notifications/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(navigator.serviceWorker.register).toHaveBeenCalled();
    });
    expect(Notification.requestPermission).not.toHaveBeenCalled();
    expect(getVapidPublicKey).not.toHaveBeenCalled();
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

    const button = await screen.findByRole("button", {
      name: /enable notifications/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(
        "Failed to subscribe the user: ",
        expect.any(Error),
      );
    });
    consoleSpy.mockRestore();
  });

  it("should unsubscribe the user when the disable button is clicked", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.getRegistration).mockResolvedValue(
      mockRegistration,
    );
    mockRegistration.pushManager.getSubscription.mockResolvedValue(
      mockSubscription,
    );
    mockSubscription.unsubscribe.mockResolvedValue(true);

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /disable notifications/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(mockSubscription.unsubscribe).toHaveBeenCalled();
    });

    await screen.findByRole("button", { name: /enable notifications/i });
  });

  it("should handle unsubscribe when there is no existing subscription", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    // First call during mount returns a registration with a subscription so
    // the component renders the disable button; click then finds no subscription.
    mockPartial(navigator.serviceWorker.getRegistration).mockResolvedValue(
      mockRegistration,
    );
    mockRegistration.pushManager.getSubscription
      .mockResolvedValueOnce(mockSubscription)
      .mockResolvedValueOnce(null);

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /disable notifications/i,
    });
    await userEvent.click(button);

    await screen.findByRole("button", { name: /enable notifications/i });
    expect(mockSubscription.unsubscribe).not.toHaveBeenCalled();
  });

  it("should handle unsubscribe when there is no existing registration", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    // First call during mount returns a registration with a subscription so
    // the component renders the disable button; click then finds no subscription.
    mockPartial(navigator.serviceWorker.getRegistration)
      .mockResolvedValueOnce(mockRegistration)
      .mockResolvedValue(undefined);
    mockRegistration.pushManager.getSubscription.mockResolvedValue(
      mockSubscription,
    );

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /disable notifications/i,
    });
    await userEvent.click(button);

    await screen.findByRole("button", { name: /enable notifications/i });
    expect(mockSubscription.unsubscribe).not.toHaveBeenCalled();
  });

  it("should handle failed unsubscription", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.getRegistration)
      .mockResolvedValueOnce(mockRegistration)
      .mockRejectedValueOnce(new Error("Unsubscribe failed"));
    mockRegistration.pushManager.getSubscription.mockResolvedValue(
      mockSubscription,
    );

    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /disable notifications/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(
        "Failed to unsubscribe the user: ",
        expect.any(Error),
      );
    });
    consoleSpy.mockRestore();
  });

  it("should log the subscription-state read error on mount", async () => {
    mockPartial(useUser).mockReturnValue({
      user: { name: "Test User" },
      isLoading: false,
    });
    mockPartial(navigator.serviceWorker.getRegistration).mockRejectedValue(
      new Error("Read failed"),
    );
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    render(<NotificationManager />);

    await waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(
        "Failed to read push subscription state: ",
        expect.any(Error),
      );
    });
    consoleSpy.mockRestore();
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

    const button = await screen.findByRole("button", {
      name: /enable notifications/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(getVapidPublicKey).toHaveBeenCalled();
    });
    expect(mockRegistration.pushManager.subscribe).not.toHaveBeenCalled();
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
    mockRegistration.pushManager.subscribe.mockResolvedValue(mockSubscription);
    mockSubscription.getKey.mockReturnValue(null);

    render(<NotificationManager />);

    const button = await screen.findByRole("button", {
      name: /enable notifications/i,
    });
    await userEvent.click(button);

    await waitFor(() => {
      expect(registerSubscription).toHaveBeenCalledWith(
        expect.objectContaining({
          body: expect.objectContaining({
            endpoint: mockSubscription.endpoint,
            keys: { p256dh: "", auth: "" },
          }),
        }),
      );
    });
  });
});
