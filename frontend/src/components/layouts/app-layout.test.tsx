import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { AppLayout } from "./app-layout";

vi.mock("@/components/navbar", () => ({
  Navbar: () => <div data-testid="navbar">Mocked Navbar</div>,
}));

vi.mock("@/components/user/api-version", () => ({
  ApiVersion: () => <div data-testid="api-version">Mocked API Version</div>,
}));

describe("AppLayout", () => {
  it("should render the layout with navbar, children and footer", () => {
    render(
      <AppLayout>
        <div data-testid="child">Child Content</div>
      </AppLayout>,
    );

    expect(screen.getByTestId("navbar")).toBeInTheDocument();
    expect(screen.getByTestId("child")).toBeInTheDocument();
    expect(screen.getByRole("contentinfo")).toBeInTheDocument();
    expect(screen.getByTestId("api-version")).toBeInTheDocument();
    expect(screen.getByText(/All rights reserved/)).toBeInTheDocument();
  });
});
