import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { UserProfile } from "@/generated";
import { UserProfileClient } from "./user-profile-client";

vi.mock("./update-user-profile-form", () => ({
  UpdateUserProfileForm: ({ onCancel }: { onCancel: () => void }) => (
    <div data-testid="update-form">
      Mocked Form
      <button type={"submit"} onClick={onCancel}>
        Cancel
      </button>
    </div>
  ),
}));

const mockUserProfile: UserProfile = {
  id: "user-123",
  auth0Id: "auth0|123",
  email: "test@example.com",
  nickname: "Test User",
  useGravatar: true,
  households: [],
};

describe("UserProfileClient", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  it("renders Edit Profile button by default", () => {
    render(<UserProfileClient userProfile={mockUserProfile} />);

    expect(
      screen.getByRole("button", { name: /Edit Profile/i }),
    ).toBeInTheDocument();
    expect(screen.queryByTestId("update-form")).not.toBeInTheDocument();
  });

  it("shows update form when Edit Profile is clicked", async () => {
    const user = userEvent.setup();
    render(<UserProfileClient userProfile={mockUserProfile} />);

    await user.click(screen.getByRole("button", { name: /Edit Profile/i }));

    expect(screen.getByTestId("update-form")).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: /Edit Profile/i }),
    ).not.toBeInTheDocument();
  });

  it("hides form and shows Edit Profile button when onCancel is called", async () => {
    const user = userEvent.setup();
    render(<UserProfileClient userProfile={mockUserProfile} />);

    await user.click(screen.getByRole("button", { name: /Edit Profile/i }));
    await user.click(screen.getByRole("button", { name: /Cancel/i }));

    expect(
      screen.getByRole("button", { name: /Edit Profile/i }),
    ).toBeInTheDocument();
    expect(screen.queryByTestId("update-form")).not.toBeInTheDocument();
  });
});
