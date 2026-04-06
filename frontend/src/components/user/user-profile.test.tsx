import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { getCurrentUser } from "@/generated";
import { auth0 } from "@/lib/auth0";
import { mockPartial } from "@/lib/test-utils";
import { UserProfileCard } from "./user-profile";

vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn(),
  },
}));
vi.mock("@/generated/sdk.gen", () => ({
  getCurrentUser: vi.fn(),
}));

describe("UserProfileCard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("console", { ...console, error: vi.fn() });
  });
  afterEach(() => {
    cleanup();
  });
  it("should return null when no session exists", async () => {
    vi.mocked(auth0.getSession).mockResolvedValue(null);

    expect.assertions(1);
    expect(await UserProfileCard()).toBeNull();
  });
  it("should return null when session contains no user", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({});

    expect.assertions(1);
    expect(await UserProfileCard()).toBeNull();
  });
  it("should render user info and householdEntity info when session exists", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({
      user: {
        name: "John Doe",
        email: "john@example.com",
      },
    });
    mockPartial(getCurrentUser).mockResolvedValue({
      data: {
        id: "user-uuid",
        auth0Id: "auth0-uuid",
        households: [{ id: "h1", name: "My House", role: "Owner" }],
      },
    });

    render(await UserProfileCard());

    expect.assertions(5); // Ensures all assertions execute
    expect(screen.getByText("John Doe")).toBeInTheDocument();
    expect(screen.getByText("john@example.com")).toBeInTheDocument();
    expect(screen.getByText("My House")).toBeInTheDocument();
    expect(screen.getByText(/Owner/i)).toBeInTheDocument();
    expect(screen.getByText(/ID: user-uuid/)).toBeInTheDocument();
  });
  it("should show 'No household assigned' when user has no households", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({ user: {} });
    mockPartial(getCurrentUser).mockResolvedValue({
      data: {
        households: [],
      },
    });

    render(await UserProfileCard());

    expect.assertions(1);
    expect(screen.getByText(/No household assigned/i)).toBeInTheDocument();
  });
  it("should log an error and handle fetch failure", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({
      user: {
        name: "Error User",
      },
    });
    vi.mocked(getCurrentUser).mockRejectedValue(new Error("Network error"));

    render(await UserProfileCard());

    expect.assertions(3);
    expect(console.error).toHaveBeenCalledWith(
      "Failed to fetch user mapping:",
      new Error("Network error"),
    );
    expect(screen.getByText("Error User")).toBeInTheDocument();
    expect(screen.queryByText(/ID:/)).not.toBeInTheDocument();
  });
  it("should show default alt text for image when user.name is undefined", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({
      user: {
        name: undefined,
        picture: "https://example.com/avatar.jpg",
      },
    });

    render(await UserProfileCard());

    expect(screen.getByRole("img")).toHaveAttribute(
      "alt",
      "User Profile Picture",
    );
  });
});
