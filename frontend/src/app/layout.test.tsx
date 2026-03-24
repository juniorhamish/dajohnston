import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import RootLayout from "./layout";

vi.mock("next/font/google", () => ({
  Geist: () => ({ variable: "geist-sans" }),
  Geist_Mono: () => ({ variable: "geist-mono" }),
}));

vi.mock("@auth0/nextjs-auth0/client", () => ({
  Auth0Provider: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="auth0-provider">{children}</div>
  ),
}));

describe("RootLayout", () => {
  it("should render the layout with children and Auth0Provider", () => {
    render(
      <RootLayout>
        <div data-testid="child">Child Content</div>
      </RootLayout>,
    );

    expect(screen.getByTestId("auth0-provider")).toBeInTheDocument();
    expect(screen.getByTestId("child")).toBeInTheDocument();
    expect(screen.getByText("Child Content")).toBeInTheDocument();
  });
});
