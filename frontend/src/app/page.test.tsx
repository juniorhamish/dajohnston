import { render, screen, within } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import Home from "./page";

vi.mock("@/components/layouts/app-layout", () => ({
  AppLayout: vi.fn(({ children }: { children: React.ReactNode }) => (
    <div data-testid="app-layout">
      <main>{children}</main>
      <footer role="contentinfo">
        <div data-testid="api-version">Mocked API Version</div>
      </footer>
    </div>
  )),
}));

vi.mock("@/components/user/api-version", () => ({
  ApiVersion: vi.fn(() => (
    <div data-testid="api-version">Mocked API Version</div>
  )),
}));
vi.mock("@/components/auth/auth-buttons", () => ({
  AuthButtons: vi.fn(() => (
    <div data-testid="auth-buttons">Mocked Auth Buttons</div>
  )),
}));
vi.mock("@/components/user/user-profile", () => ({
  UserProfileCard: vi.fn(() => (
    <div data-testid="user-profile">Mocked User Profile</div>
  )),
}));
vi.mock("@/components/apps/app-grid", () => ({
  AppGrid: vi.fn(() => <div data-testid="app-grid">Mocked App Grid</div>),
}));
vi.mock("@/components/invitations/pending-invitations", () => ({
  PendingInvitations: vi.fn(() => (
    <div data-testid="pending-invitations">Mocked Pending Invitations</div>
  )),
}));

describe("Home Page", () => {
  it("should render the home page with mocked subcomponents", async () => {
    render(await Home());

    const main = screen.getByRole("main");
    expect(
      within(main).getByRole("heading", {
        name: "Portal Dashboard",
      }),
    ).toBeInTheDocument();
    expect(
      within(main).getByText(
        "Welcome to your centralized hub for sub-applications.",
      ),
    ).toBeInTheDocument();
    expect(within(main).getByTestId("user-profile")).toBeInTheDocument();
    expect(within(main).getByTestId("pending-invitations")).toBeInTheDocument();
    expect(within(main).getByTestId("app-grid")).toBeInTheDocument();
    expect(
      within(screen.getByRole("contentinfo")).getByTestId("api-version"),
    ).toBeInTheDocument();
  });
});
