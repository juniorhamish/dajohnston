import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { auth0 } from "@/lib/auth0";
import { mockSession } from "@/lib/test-utils";
import { UserProfile } from "./user-profile";

vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn(),
  },
}));

// Mock global fetch
globalThis.fetch = vi.fn();
const originalApiUrl = process.env.NEXT_PUBLIC_API_URL;

describe("UserProfile", () => {
  beforeEach(() => {
    vi.clearAllMocks(); // Clear mocks before each test
    process.env.NEXT_PUBLIC_API_URL = "https://test-backend.local";
  });
  afterEach(() => {
    cleanup();
    process.env.NEXT_PUBLIC_API_URL = originalApiUrl;
  });
  it("should return null when no session exists", async () => {
    vi.mocked(auth0.getSession).mockResolvedValue(null);

    expect.assertions(1); // Ensures all assertions execute
    const component = await UserProfile();
    expect(component).toBeNull();
  });

  it("should render user info and household info when session exists", async () => {
    vi.mocked(auth0.getSession).mockResolvedValue(
      mockSession({
        user: {
          name: "John Doe",
          email: "john@example.com",
        },
      }),
    );

    vi.mocked(fetch).mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: "user-uuid",
        households: [{ id: "h1", name: "My House", role: "Owner" }],
      }),
    } as Response);

    expect.assertions(5); // Ensures all assertions execute
    const component = await UserProfile();
    render(component);

    expect(screen.getByText("John Doe")).toBeInTheDocument();
    expect(screen.getByText("john@example.com")).toBeInTheDocument();
    expect(screen.getByText("My House")).toBeInTheDocument();
    expect(screen.getByText(/Owner/i)).toBeInTheDocument();
    expect(screen.getByText(/ID: user-uuid/)).toBeInTheDocument();
  });

  it("should show 'No household assigned' when user has no households", async () => {
    vi.mocked(auth0.getSession).mockResolvedValue(mockSession());

    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: async () => ({
        id: "jane-uuid",
        households: [],
      }),
    } as Response);

    const component = await UserProfile();
    render(component);

    expect.assertions(1); // Ensures the assertion is reached
    expect(screen.getByText(/No household assigned/i)).toBeInTheDocument();
  });

  it("should log an error and handle fetch failure", async () => {
    vi.mocked(auth0.getSession).mockResolvedValue(
      mockSession({
        user: {
          name: "Error User",
        },
      }),
    );

    const error = new Error("Network error");
    vi.mocked(fetch).mockRejectedValue(error);
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    expect.assertions(3);

    const component = await UserProfile();
    render(component);

    expect(consoleSpy).toHaveBeenCalledWith(
      "Failed to fetch user mapping:",
      error,
    );
    expect(screen.getByText("Error User")).toBeInTheDocument();
    expect(screen.queryByText(/ID:/)).not.toBeInTheDocument();

    consoleSpy.mockRestore();
  });

  it("should show default alt text for image when user.name is undefined", async () => {
    vi.mocked(auth0.getSession).mockResolvedValue(
      mockSession({
        user: {
          name: undefined,
        },
      }),
    );

    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: async () => ({
        id: "id",
        households: [],
      }),
    } as Response);

    const component = await UserProfile();
    render(component);

    const img = screen.getByRole("img");
    expect(img).toHaveAttribute("alt", "User Profile Picture");
  });

  it("should use the environment variable NEXT_PUBLIC_API_URL when set", async () => {
    const customUrl = "https://custom-backend.local";
    process.env.NEXT_PUBLIC_API_URL = customUrl;

    vi.mocked(auth0.getSession).mockResolvedValue(
      mockSession({
        tokenSet: { accessToken: "test-token" },
      }),
    );

    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: async () => ({ id: "id", households: [] }),
    } as Response);

    await UserProfile();

    expect(fetch).toHaveBeenCalledWith(
      `${customUrl}/api/users/me`,
      expect.objectContaining({
        headers: expect.any(Headers),
      }),
    );

    const callHeaders = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers;
    expect(callHeaders.get("Authorization")).toBe("Bearer test-token");
  });

  it("should use the default backend URL when NEXT_PUBLIC_API_URL is not set", async () => {
    delete process.env.NEXT_PUBLIC_API_URL;

    vi.mocked(auth0.getSession).mockResolvedValue(
      mockSession({
        tokenSet: { accessToken: "test-token" },
      }),
    );

    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: async () => ({ id: "id", households: [] }),
    } as Response);

    await UserProfile();

    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/api/users/me",
      expect.objectContaining({
        headers: expect.any(Headers),
      }),
    );

    const callHeaders = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers;
    expect(callHeaders.get("Authorization")).toBe("Bearer test-token");
  });
});
