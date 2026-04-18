import { render, screen, within } from "@testing-library/react";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";
import RootLayout from "./layout";

vi.mock("next/font/google", () => ({
  Geist: () => ({ variable: "geist-sans" }),
  Geist_Mono: () => ({ variable: "geist-mono" }),
}));

vi.mock("@auth0/nextjs-auth0/client", () => ({
  Auth0Provider: ({ children }: { children: ReactNode }) => (
    <div data-testid="auth0-provider">{children}</div>
  ),
  useUser: () => ({ user: null, isLoading: false }),
}));

vi.mock("@/components/notifications/notification-manager", () => ({
  NotificationManager: () => <div data-testid="notification-manager" />,
}));

describe("RootLayout", () => {
  it("should render the layout with children and Auth0Provider", () => {
    render(
      <RootLayout>
        <div data-testid="child">Child Content</div>
      </RootLayout>,
    );

    const auth0ProviderComponent = screen.getByTestId("auth0-provider");
    expect(auth0ProviderComponent).toBeInTheDocument();
    expect(
      within(auth0ProviderComponent).getByTestId("child"),
    ).toBeInTheDocument();
    expect(
      within(auth0ProviderComponent).getByTestId("child"),
    ).toBeInTheDocument();
    expect(
      within(auth0ProviderComponent).getByText("Child Content"),
    ).toBeInTheDocument();
    expect(
      within(auth0ProviderComponent).getByTestId("notification-manager"),
    ).toBeInTheDocument();
  });
});
