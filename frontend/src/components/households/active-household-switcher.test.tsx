import { render, screen } from "@testing-library/react";
import { cookies } from "next/headers";
import type { ComponentProps } from "react";
import { describe, expect, it, vi } from "vitest";
import type { HouseholdSwitcher } from "@/components/households/household-switcher";
import { getCurrentUser } from "@/generated";
import { mockPartial } from "@/lib/test-utils";
import { ActiveHouseholdSwitcher } from "./active-household-switcher";

vi.mock("next/headers", () => ({
  cookies: vi.fn(),
}));

vi.mock("@/generated", () => ({
  getCurrentUser: vi.fn(),
}));

vi.mock("./household-switcher", () => ({
  HouseholdSwitcher: ({
    households,
    activeHouseholdId,
  }: ComponentProps<typeof HouseholdSwitcher>) => (
    <div data-testid="switcher">
      {households.length} houses, active: {activeHouseholdId}
    </div>
  ),
}));

describe("ActiveHouseholdSwitcher", () => {
  it("should render null if no user", async () => {
    mockPartial(cookies).mockResolvedValue({ get: () => undefined });
    mockPartial(getCurrentUser).mockResolvedValue({ data: undefined });

    const result = await ActiveHouseholdSwitcher();
    expect(result).toBeNull();
  });
  it("should render null if request to get user fails", async () => {
    mockPartial(cookies).mockResolvedValue({ get: () => undefined });
    mockPartial(getCurrentUser).mockRejectedValue({ data: undefined });

    const result = await ActiveHouseholdSwitcher();
    expect(result).toBeNull();
  });
  it("should render switcher with data", async () => {
    mockPartial(cookies).mockResolvedValue({
      get: () => ({ name: "selected_household_id", value: "h1" }),
    });
    mockPartial(getCurrentUser).mockResolvedValue({
      data: { households: [{ id: "h1", name: "H1" }] },
    });

    render(await ActiveHouseholdSwitcher());

    expect(screen.getByTestId("switcher")).toHaveTextContent(
      "1 houses, active: h1",
    );
  });
});
