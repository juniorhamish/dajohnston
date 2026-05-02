import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { setActiveHousehold } from "@/app/actions/household";
import { SetActiveHouseholdButton } from "./set-active-household-button";

vi.mock("@/app/actions/household", () => ({
  setActiveHousehold: vi.fn(),
}));

describe("SetActiveHouseholdButton", () => {
  it("should render Active state when isActive is true", () => {
    render(<SetActiveHouseholdButton householdId="h1" isActive={true} />);

    expect(screen.getByText("Active")).toBeInTheDocument();
  });

  it("should render Set Active button when isActive is false", () => {
    render(<SetActiveHouseholdButton householdId="h1" isActive={false} />);

    expect(screen.getByText("Set Active")).toBeInTheDocument();
  });

  it("should call setActiveHousehold on click", () => {
    render(<SetActiveHouseholdButton householdId="h1" isActive={false} />);

    fireEvent.click(screen.getByText("Set Active"));

    expect(setActiveHousehold).toHaveBeenCalledWith("h1");
  });
});
