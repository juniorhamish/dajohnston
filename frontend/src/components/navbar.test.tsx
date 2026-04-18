import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { Navbar } from "./navbar";

vi.mock("@/components/auth/auth-buttons", () => ({
  AuthButtons: () => <div data-testid="auth-buttons">Auth Buttons</div>,
}));

vi.mock("@/components/theme-toggle", () => ({
  ThemeToggle: () => <div data-testid="theme-toggle">Theme Toggle</div>,
}));

describe("Navbar", () => {
  it("should render the navbar with logo and navigation links", () => {
    render(<Navbar />);

    expect(screen.getByText("Multi-App Portal")).toBeInTheDocument();
    expect(screen.getByText("Dashboard")).toBeInTheDocument();
    expect(screen.getByTestId("theme-toggle")).toBeInTheDocument();
    expect(screen.getByTestId("auth-buttons")).toBeInTheDocument();
  });
});
