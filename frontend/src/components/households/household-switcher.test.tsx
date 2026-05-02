import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { setActiveHousehold } from "@/app/actions/household";
import { HouseholdSwitcher } from "./household-switcher";

vi.mock("@/app/actions/household", () => ({
  setActiveHousehold: vi.fn(),
}));

describe("HouseholdSwitcher", () => {
  const households = [
    { id: "h1", name: "House 1", role: "OWNER" as const },
    { id: "h2", name: "House 2", role: "MEMBER" as const },
  ];

  it("should render null if no households", () => {
    const { container } = render(<HouseholdSwitcher households={[]} />);
    expect(container).toBeEmptyDOMElement();
  });

  it("should render households in select", () => {
    render(
      <HouseholdSwitcher households={households} activeHouseholdId="h1" />,
    );

    expect(screen.getByText("House 1")).toBeInTheDocument();
    expect(screen.getByText("House 2")).toBeInTheDocument();
    expect(screen.getByDisplayValue("House 1")).toBeInTheDocument();
  });

  it("should call setActiveHousehold on change", () => {
    render(<HouseholdSwitcher households={households} />);

    fireEvent.change(screen.getByRole("combobox"), { target: { value: "h2" } });

    expect(setActiveHousehold).toHaveBeenCalledWith("h2");
  });
});
