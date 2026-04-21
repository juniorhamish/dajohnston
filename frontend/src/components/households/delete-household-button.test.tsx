import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { DeleteHouseholdButton } from "./delete-household-button";
import { deleteHouseholdAction } from "./household-actions";

vi.mock("./household-actions", () => ({
  deleteHouseholdAction: vi.fn(),
}));

describe("DeleteHouseholdButton", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("confirm", vi.fn());
    vi.stubGlobal("alert", vi.fn());
  });

  it("should render the delete button", () => {
    render(
      <DeleteHouseholdButton householdId="h1" householdName="Test Household" />,
    );
    expect(
      screen.getByRole("button", { name: /delete household/i }),
    ).toBeInTheDocument();
  });

  it("should call deleteHouseholdAction when confirmed", async () => {
    vi.mocked(window.confirm).mockReturnValue(true);
    const user = userEvent.setup();

    render(
      <DeleteHouseholdButton householdId="h1" householdName="Test Household" />,
    );
    await user.click(screen.getByRole("button", { name: /delete household/i }));

    expect(window.confirm).toHaveBeenCalledWith(
      expect.stringContaining(
        'Are you sure you want to delete the household "Test Household"?',
      ),
    );
    expect(deleteHouseholdAction).toHaveBeenCalledWith("h1");
  });

  it("should not call deleteHouseholdAction when cancelled", async () => {
    vi.mocked(window.confirm).mockReturnValue(false);
    const user = userEvent.setup();

    render(
      <DeleteHouseholdButton householdId="h1" householdName="Test Household" />,
    );
    await user.click(screen.getByRole("button", { name: /delete household/i }));

    expect(window.confirm).toHaveBeenCalled();
    expect(deleteHouseholdAction).not.toHaveBeenCalled();
  });

  it("should show alert when delete fails", async () => {
    vi.mocked(window.confirm).mockReturnValue(true);
    vi.mocked(deleteHouseholdAction).mockRejectedValue(new Error("Failed"));
    const user = userEvent.setup();

    render(
      <DeleteHouseholdButton householdId="h1" householdName="Test Household" />,
    );
    await user.click(screen.getByRole("button", { name: /delete household/i }));

    expect(window.alert).toHaveBeenCalledWith(
      "Failed to delete household. Please try again.",
    );
  });
});
