import { act, cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { mockPartial } from "@/lib/test-utils";
import { CreateHouseholdForm } from "./create-household-form";
import { createHouseholdAction } from "./household-actions";

vi.mock("./household-actions", () => ({
  createHouseholdAction: vi.fn(),
}));

describe("CreateHouseholdForm", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("console", { ...console, error: vi.fn() });
  });

  afterEach(() => {
    cleanup();
  });

  it("should initially render only the create button", () => {
    render(<CreateHouseholdForm />);

    expect(screen.getByText("+ Create New Household")).toBeInTheDocument();
    expect(screen.queryByLabelText(/Household Name/i)).not.toBeInTheDocument();
  });

  it("should show the form when create button is clicked", async () => {
    const user = userEvent.setup();
    render(<CreateHouseholdForm />);

    await user.click(screen.getByText("+ Create New Household"));

    expect(screen.getByText("Create New Household")).toBeInTheDocument();
    expect(screen.getByLabelText(/Household Name/i)).toBeInTheDocument();
  });

  it("should close the form when cancel is clicked", async () => {
    const user = userEvent.setup();
    render(<CreateHouseholdForm />);

    await user.click(screen.getByText("+ Create New Household"));
    await user.click(screen.getByText("Cancel"));

    expect(screen.getByText("+ Create New Household")).toBeInTheDocument();
    expect(
      screen.queryByText("Create New Household", { selector: "h4" }),
    ).not.toBeInTheDocument();
  });

  it("should call createHouseholdAction on submit", async () => {
    const user = userEvent.setup();
    render(<CreateHouseholdForm />);

    await user.click(screen.getByText("+ Create New Household"));

    const nameInput = screen.getByLabelText(/Household Name/i);
    const submitButton = screen.getByText("Create Household");

    await user.type(nameInput, "New Home");

    // Mocking the action response
    vi.mocked(createHouseholdAction).mockResolvedValue(undefined);

    await user.click(submitButton);

    // Wait for the action to be called
    expect(createHouseholdAction).toHaveBeenCalledWith("New Home");
  });

  it("should log error and stay open if createHouseholdAction fails", async () => {
    const user = userEvent.setup();
    mockPartial(createHouseholdAction).mockRejectedValue(
      new Error("API Error"),
    );

    render(<CreateHouseholdForm />);

    await user.click(screen.getByText("+ Create New Household"));

    const nameInput = screen.getByLabelText(/Household Name/i);
    const submitButton = screen.getByText("Create Household");

    await user.type(nameInput, "New Home");
    await user.click(submitButton);

    // Wait for the action to be called
    await vi.waitFor(() => expect(createHouseholdAction).toHaveBeenCalled());

    // Verify error was logged
    expect(console.error).toHaveBeenCalledWith(
      "Failed to create household:",
      expect.any(Error),
    );

    // Verify it's still open and showing the form
    expect(
      screen.getByText("Create New Household", { selector: "h4" }),
    ).toBeInTheDocument();

    // Verify it's no longer pending
    await vi.waitFor(() => {
      expect(screen.getByText("Create Household")).not.toBeDisabled();
    });
  });

  it("should show loading state while createHouseholdAction is pending", async () => {
    const user = userEvent.setup();
    let resolveAction!: (value: void | PromiseLike<void>) => void;
    const actionPromise = new Promise<void>((resolve) => {
      resolveAction = resolve;
    });
    mockPartial(createHouseholdAction).mockReturnValue(actionPromise);

    render(<CreateHouseholdForm />);

    await user.click(screen.getByText("+ Create New Household"));

    const nameInput = screen.getByLabelText(/Household Name/i);
    await user.type(nameInput, "New Home");

    const submitButton = screen.getByText("Create Household");

    // Trigger submit
    await user.click(submitButton);

    // Verify loading state
    await vi.waitFor(() => {
      expect(
        screen.getByRole("button", { name: "Creating..." }),
      ).toBeInTheDocument();
      expect(
        screen.getByRole("button", { name: "Creating..." }),
      ).toBeDisabled();
    });

    // Resolve the promise
    await act(async () => {
      resolveAction();
    });

    // Verify it finishes and form closes (it goes back to initial state)
    await vi.waitFor(() =>
      expect(screen.getByText("+ Create New Household")).toBeInTheDocument(),
    );
  });
});
