import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import Home from "./page";

vi.mock("@/components/api-version", () => ({
  ApiVersion: vi.fn(() => (
    <div data-testid="api-version">Mocked API Version</div>
  )),
}));

vi.mock("@/components/auth-buttons", () => ({
  AuthButtons: vi.fn(() => (
    <div data-testid="auth-buttons">Mocked Auth Buttons</div>
  )),
}));

vi.mock("@/components/user-profile", () => ({
  UserProfile: vi.fn(() => (
    <div data-testid="user-profile">Mocked User Profile</div>
  )),
}));

describe("Home Page", () => {
  it("should render the home page with mocked subcomponents", async () => {
    const component = await Home();
    render(component);

    expect(screen.getByText("Multi-App Portal")).toBeInTheDocument();
    expect(
      screen.getByText("Welcome to your centralized hub for sub-applications."),
    ).toBeInTheDocument();
    expect(screen.getByTestId("api-version")).toBeInTheDocument();
    expect(screen.getByTestId("auth-buttons")).toBeInTheDocument();
    expect(screen.getByTestId("user-profile")).toBeInTheDocument();
  });
});
