import {
  act,
  cleanup,
  fireEvent,
  render,
  screen,
} from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { mockPartial } from "@/lib/test-utils";
import { inviteUserAction } from "./invitation-actions";
import { InviteUserForm } from "./invite-user-form";

vi.mock("./invitation-actions", () => ({
  inviteUserAction: vi.fn(),
}));

describe("InviteUserForm", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("console", { ...console, error: vi.fn() });
  });

  afterEach(() => {
    cleanup();
  });

  it("should initially render only the invite button", () => {
    render(<InviteUserForm householdId="h1" householdName="My House" />);

    expect(screen.getByText("+ Invite User")).toBeInTheDocument();
    expect(screen.queryByLabelText(/Email Address/i)).not.toBeInTheDocument();
  });

  it("should show the form when invite button is clicked", () => {
    render(<InviteUserForm householdId="h1" householdName="My House" />);

    fireEvent.click(screen.getByText("+ Invite User"));

    expect(screen.getByText("Invite to My House")).toBeInTheDocument();
    expect(screen.getByLabelText(/Email Address/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Role/i)).toBeInTheDocument();
  });

  it("should close the form when cancel is clicked", () => {
    render(<InviteUserForm householdId="h1" householdName="My House" />);

    fireEvent.click(screen.getByText("+ Invite User"));
    fireEvent.click(screen.getByText("Cancel"));

    expect(screen.getByText("+ Invite User")).toBeInTheDocument();
    expect(screen.queryByText("Invite to My House")).not.toBeInTheDocument();
  });

  it("should call inviteUserAction on submit", async () => {
    render(<InviteUserForm householdId="h1" householdName="My House" />);

    fireEvent.click(screen.getByText("+ Invite User"));

    const emailInput = screen.getByLabelText(/Email Address/i);
    const roleSelect = screen.getByLabelText(/Role/i);
    const submitButton = screen.getByText("Send Invitation");

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.change(roleSelect, { target: { value: "OWNER" } });

    // We can't easily fireEvent.submit on a form with Server Action in Vitest JSDOM without more setup,
    // but since we're using a standard form with 'action', we can try to find the button and click it.
    // However, handleSubmit is an async function passed to action.

    // Mocking the action response
    vi.mocked(inviteUserAction).mockResolvedValue(undefined);

    fireEvent.click(submitButton);

    // In a real environment, the form action would be triggered.
    // In our component, we have 'action={handleSubmit}'.
    // fireEvent.click(submitButton) should trigger it.

    // Wait for the action to be called
    expect(inviteUserAction).toHaveBeenCalled();
    const [householdId, formData] = vi.mocked(inviteUserAction).mock.calls[0];
    expect(householdId).toBe("h1");
    expect(formData.get("email")).toBe("test@example.com");
    expect(formData.get("role")).toBe("OWNER");
  });

  it("should log error and stay open if inviteUserAction fails", async () => {
    mockPartial(inviteUserAction).mockRejectedValue(new Error("API Error"));

    render(<InviteUserForm householdId="h1" householdName="My House" />);

    fireEvent.click(screen.getByText("+ Invite User"));

    const emailInput = screen.getByLabelText(/Email Address/i);
    const submitButton = screen.getByText("Send Invitation");

    fireEvent.change(emailInput, { target: { value: "test@example.com" } });
    fireEvent.click(submitButton);

    // Wait for the action to be called
    await vi.waitFor(() => expect(inviteUserAction).toHaveBeenCalled());

    // Verify error was logged
    expect(console.error).toHaveBeenCalledWith(
      "Failed to invite user:",
      expect.any(Error),
    );

    // Verify it's still open and showing the form
    expect(screen.getByText("Invite to My House")).toBeInTheDocument();

    // Verify it's no longer pending
    await vi.waitFor(() => {
      expect(screen.getByText("Send Invitation")).not.toBeDisabled();
    });
  });

  it("should show loading state while inviteUserAction is pending", async () => {
    let resolveAction!: (value: void | PromiseLike<void>) => void;
    const actionPromise = new Promise<void>((resolve) => {
      resolveAction = resolve;
    });
    mockPartial(inviteUserAction).mockReturnValue(actionPromise);

    render(<InviteUserForm householdId="h1" householdName="My House" />);

    fireEvent.click(screen.getByText("+ Invite User"));

    const emailInput = screen.getByLabelText(/Email Address/i);
    fireEvent.change(emailInput, { target: { value: "test@example.com" } });

    const submitButton = screen.getByText("Send Invitation");

    // Trigger submit
    fireEvent.click(submitButton);

    // Verify loading state
    await vi.waitFor(() => {
      expect(
        screen.getByRole("button", { name: "Sending..." }),
      ).toBeInTheDocument();
      expect(screen.getByRole("button", { name: "Sending..." })).toBeDisabled();
    });

    // Resolve the promise
    await act(async () => {
      resolveAction();
    });

    // Verify it finishes and form closes (it goes back to initial state)
    await vi.waitFor(() =>
      expect(screen.getByText("+ Invite User")).toBeInTheDocument(),
    );
  });
});
