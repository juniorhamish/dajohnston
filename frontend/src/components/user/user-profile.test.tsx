import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { DeleteHouseholdButtonProps } from "@/components/households/delete-household-button";
import type { InviteUserFormProps } from "@/components/invitations/invite-user-form";
import { getCurrentUser } from "@/generated";
import { auth0 } from "@/lib/auth0";
import { mockPartial } from "@/lib/test-utils";
import { UserProfileCard } from "./user-profile";

vi.mock("next/headers", () => ({
  cookies: vi.fn().mockResolvedValue({
    get: vi.fn(),
  }),
}));
vi.mock("@/components/households/set-active-household-button", () => ({
  SetActiveHouseholdButton: () => <div data-testid="set-active-button" />,
}));
vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn(),
  },
}));
vi.mock("@/generated/sdk.gen", () => ({
  getCurrentUser: vi.fn(),
}));
vi.mock("@/components/invitations/invite-user-form", () => ({
  InviteUserForm: ({ householdName }: InviteUserFormProps) => (
    <div data-testid="invite-form">Invite to {householdName}</div>
  ),
}));
vi.mock("@/components/households/delete-household-button", () => ({
  DeleteHouseholdButton: ({ householdName }: DeleteHouseholdButtonProps) => (
    <div data-testid="delete-button">Delete {householdName}</div>
  ),
}));
vi.mock("./user-profile-client", () => ({
  UserProfileClient: () => (
    <div data-testid="user-profile-client">Edit Profile</div>
  ),
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
        households: [{ id: "h1", name: "My House", role: "OWNER" }],
      },
    });

    render(await UserProfileCard());

    expect.assertions(9); // Ensures all assertions execute
    expect(screen.getByText("John Doe")).toBeInTheDocument();
    expect(screen.getByText("john@example.com")).toBeInTheDocument();
    expect(screen.getByText("My House")).toBeInTheDocument();
    expect(screen.getByText(/Owner/i)).toBeInTheDocument();
    expect(screen.getByText(/ID: user-uuid/)).toBeInTheDocument();
    expect(screen.getByTestId("invite-form")).toBeInTheDocument();
    expect(screen.getByText("Invite to My House")).toBeInTheDocument();
    expect(screen.getByTestId("delete-button")).toBeInTheDocument();
    expect(screen.getByText("Delete My House")).toBeInTheDocument();
  });
  it("should not show invite form when user is not OWNER", async () => {
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
        households: [{ id: "h1", name: "My House", role: "MEMBER" }],
      },
    });

    render(await UserProfileCard());

    expect(screen.getByText("My House")).toBeInTheDocument();
    expect(screen.getByText(/Member/i)).toBeInTheDocument();
    expect(screen.queryByTestId("invite-form")).not.toBeInTheDocument();
    expect(screen.queryByTestId("delete-button")).not.toBeInTheDocument();
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
  it("renders extended profile fields when available", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({
      user: {
        name: "Old Name",
        email: "test@example.com",
        picture: "https://old.com/pic.jpg",
      },
    });

    mockPartial(getCurrentUser).mockResolvedValue({
      data: {
        id: "123",
        auth0Id: "auth|123",
        email: "test@example.com",
        givenName: "John",
        familyName: "Doe",
        nickname: "jdoe",
        picture: "https://new.com/pic.jpg",
        households: [],
      },
    });

    render(await UserProfileCard());

    expect(screen.getByText("John Doe")).toBeInTheDocument();
    expect(screen.getByText("@jdoe")).toBeInTheDocument();
    const image = screen.getByAltText("jdoe");
    expect(image).toHaveAttribute(
      "src",
      expect.stringContaining("new.com%2Fpic.jpg"),
    );
  });
});
