import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { UserProfile } from "@/generated";
import { mockPartial } from "@/lib/test-utils";
import { UpdateUserProfileForm } from "./update-user-profile-form";
import { updateUserProfileAction } from "./user-actions";

vi.mock("./user-actions", () => ({
  updateUserProfileAction: vi.fn(),
}));

const mockUserProfile: UserProfile = {
  id: "user-123",
  auth0Id: "auth0|123",
  email: "test@example.com",
  givenName: "John",
  familyName: "Doe",
  nickname: "jdoe",
  picture: "https://example.com/pic.jpg",
  useGravatar: false,
  households: [],
};

describe("UpdateUserProfileForm", () => {
  const onCancel = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  it("renders with initial values", () => {
    render(
      <UpdateUserProfileForm
        userProfile={mockUserProfile}
        onCancel={onCancel}
      />,
    );

    expect(screen.getByLabelText(/Given Name/i)).toHaveValue("John");
    expect(screen.getByLabelText(/Family Name/i)).toHaveValue("Doe");
    expect(screen.getByLabelText(/Nickname/i)).toHaveValue("jdoe");
    expect(screen.getByLabelText(/Profile Picture URL/i)).toHaveValue(
      "https://example.com/pic.jpg",
    );
    expect(screen.getByLabelText(/Use Gravatar/i)).not.toBeChecked();
  });

  it("renders with undefined values in userProfile", () => {
    const minimalProfile: UserProfile = {
      id: "user-123",
      auth0Id: "auth0|123",
      email: "test@example.com",
      givenName: undefined,
      familyName: undefined,
      nickname: undefined,
      picture: undefined,
      useGravatar: false,
      households: [],
    };
    render(
      <UpdateUserProfileForm
        userProfile={minimalProfile}
        onCancel={onCancel}
      />,
    );

    expect(screen.getByLabelText(/Given Name/i)).toHaveValue("");
    expect(screen.getByLabelText(/Family Name/i)).toHaveValue("");
    expect(screen.getByLabelText(/Nickname/i)).toHaveValue("");
    expect(screen.getByLabelText(/Profile Picture URL/i)).toHaveValue("");
  });

  it("disables picture input when useGravatar is checked", async () => {
    const user = userEvent.setup();
    render(
      <UpdateUserProfileForm
        userProfile={mockUserProfile}
        onCancel={onCancel}
      />,
    );

    const checkbox = screen.getByLabelText(/Use Gravatar/i);
    const pictureInput = screen.getByLabelText(/Profile Picture URL/i);

    expect(pictureInput).not.toBeDisabled();

    await user.click(checkbox);

    expect(checkbox).toBeChecked();
    expect(pictureInput).toBeDisabled();
  });

  it("calls onCancel when cancel button is clicked", async () => {
    const user = userEvent.setup();
    render(
      <UpdateUserProfileForm
        userProfile={mockUserProfile}
        onCancel={onCancel}
      />,
    );

    await user.click(screen.getByRole("button", { name: /Cancel/i }));

    expect(onCancel).toHaveBeenCalled();
  });

  it("submits the form with correct data", async () => {
    render(
      <UpdateUserProfileForm
        userProfile={mockUserProfile}
        onCancel={onCancel}
      />,
    );

    const form = screen.getByRole("form", { hidden: true });

    // We can't easily use userEvent with server actions in Vitest without more setup,
    // so we'll mock the form submission by calling the action directly or triggering submit.
    // However, since we're using the 'action' prop on the form, we can use fireEvent.submit
    fireEvent.submit(form);

    expect(updateUserProfileAction).toHaveBeenCalledWith(
      expect.objectContaining({
        givenName: "John",
        familyName: "Doe",
        nickname: "jdoe",
        picture: "https://example.com/pic.jpg",
        useGravatar: false,
      }),
    );
  });

  it("submits with useGravatar true and picture null", async () => {
    const user = userEvent.setup();
    render(
      <UpdateUserProfileForm
        userProfile={mockUserProfile}
        onCancel={onCancel}
      />,
    );

    await user.click(screen.getByLabelText(/Use Gravatar/i));

    fireEvent.submit(screen.getByRole("form"));

    expect(updateUserProfileAction).toHaveBeenCalledWith(
      expect.objectContaining({
        useGravatar: true,
        // picture is not present because it's null in formData and we filter it
      }),
    );
    expect(
      vi.mocked(updateUserProfileAction).mock.calls[0][0],
    ).not.toHaveProperty("picture");
  });

  it("restores manual picture URL when useGravatar is toggled off", async () => {
    const user = userEvent.setup();
    const profileWithGravatar: UserProfile = {
      ...mockUserProfile,
      useGravatar: true,
      picture: "https://gravatar.com/avatar/123",
      manualPictureUrl: "https://example.com/manual.jpg",
    };
    render(
      <UpdateUserProfileForm
        userProfile={profileWithGravatar}
        onCancel={onCancel}
      />,
    );

    const checkbox = screen.getByLabelText(/Use Gravatar/i);
    const pictureInput = screen.getByLabelText(/Profile Picture URL/i);

    expect(checkbox).toBeChecked();
    expect(pictureInput).toHaveValue("https://gravatar.com/avatar/123");
    expect(pictureInput).toBeDisabled();

    await user.click(checkbox);

    expect(checkbox).not.toBeChecked();
    expect(pictureInput).not.toBeDisabled();
    expect(pictureInput).toHaveValue("https://example.com/manual.jpg");
  });

  it("submits empty fields as undefined", async () => {
    const user = userEvent.setup();
    render(
      <UpdateUserProfileForm
        userProfile={mockUserProfile}
        onCancel={onCancel}
      />,
    );

    const givenNameInput = screen.getByLabelText(/Given Name/i);
    const familyNameInput = screen.getByLabelText(/Family Name/i);
    const nicknameInput = screen.getByLabelText(/Nickname/i);
    const pictureInput = screen.getByLabelText(/Profile Picture URL/i);

    await user.clear(givenNameInput);
    await user.clear(familyNameInput);
    await user.clear(nicknameInput);
    await user.clear(pictureInput);

    fireEvent.submit(screen.getByRole("form", { hidden: true }));

    expect(updateUserProfileAction).toHaveBeenCalledWith({
      givenName: undefined,
      familyName: undefined,
      nickname: undefined,
      useGravatar: false,
      picture: undefined,
    });
  });

  it("shows saving state when pending", async () => {
    let resolveAction!: (value: void | PromiseLike<void>) => void;
    const promise = new Promise<void>((resolve) => {
      resolveAction = resolve;
    });
    mockPartial(updateUserProfileAction).mockReturnValueOnce(promise);

    render(
      <UpdateUserProfileForm
        userProfile={mockUserProfile}
        onCancel={onCancel}
      />,
    );

    fireEvent.submit(screen.getByRole("form", { hidden: true }));

    expect(
      screen.getByRole("button", { name: /Saving.../i }),
    ).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Saving.../i })).toBeDisabled();

    resolveAction();
    await vi.waitFor(() => {
      expect(onCancel).toHaveBeenCalled();
    });
  });

  it("handles useGravatar true and undefined picture during initialization", async () => {
    const user = userEvent.setup();
    const profile: UserProfile = {
      ...mockUserProfile,
      useGravatar: true,
      picture: undefined,
      manualPictureUrl: undefined,
    };
    render(<UpdateUserProfileForm userProfile={profile} onCancel={onCancel} />);

    // useGravatar is true, picture is null -> ""
    expect(screen.getByLabelText(/Profile Picture URL/i)).toHaveValue("");

    // Toggle Gravatar off to see initialized manualPictureUrl
    const checkbox = screen.getByLabelText(/Use Gravatar/i);
    await user.click(checkbox);

    // Line 21: useGravatar was true, manualPictureUrl was undefined -> ""
    expect(screen.getByLabelText(/Profile Picture URL/i)).toHaveValue("");
  });

  it("updates manual picture URL when input changes", async () => {
    const user = userEvent.setup();
    render(
      <UpdateUserProfileForm
        userProfile={mockUserProfile}
        onCancel={onCancel}
      />,
    );

    const pictureInput = screen.getByLabelText(/Profile Picture URL/i);
    await user.clear(pictureInput);
    await user.type(pictureInput, "https://new-image.com/img.png");

    expect(pictureInput).toHaveValue("https://new-image.com/img.png");
  });

  it("logs error to console when updateUserProfileAction fails", async () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    const error = new Error("Update failed");
    vi.mocked(updateUserProfileAction).mockRejectedValueOnce(error);

    render(
      <UpdateUserProfileForm
        userProfile={mockUserProfile}
        onCancel={onCancel}
      />,
    );

    const form = screen.getByRole("form", { hidden: true });
    fireEvent.submit(form);

    await vi.waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(
        "Failed to update profile:",
        error,
      );
    });

    consoleSpy.mockRestore();
  });
});
