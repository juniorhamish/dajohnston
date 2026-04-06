import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { auth0 } from "@/lib/auth0";
import { mockPartial } from "@/lib/test-utils";
import { AuthButtons } from "./auth-buttons";

vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn(),
  },
}));

describe("AuthButtons", () => {
  it("should render the Login button when no session exists", async () => {
    vi.mocked(auth0.getSession).mockResolvedValue(null);

    render(await AuthButtons());

    const loginLink = screen.getByRole("link", { name: /Login/ });
    expect(loginLink).toBeInTheDocument();
    expect(loginLink).toHaveAttribute("href", "/auth/login");
  });
  it("should render Welcome message and Logout button when user is logged in", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({
      user: { name: "John Doe" },
    });

    render(await AuthButtons());

    expect(screen.getByText(/Welcome, John Doe/)).toBeInTheDocument();
    const logoutLink = screen.getByRole("link", { name: /logout/i });
    expect(logoutLink).toBeInTheDocument();
    expect(logoutLink).toHaveAttribute("href", "/auth/logout");
  });
});
