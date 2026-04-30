import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { listSpices } from "@/generated/spice-tracker";
import { mockPartial } from "@/lib/test-utils";
import SpiceTrackerPage from "./page";

vi.mock("@/generated/spice-tracker", () => ({
  listSpices: vi.fn(),
}));

describe("SpiceTrackerPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("console", { ...console, error: vi.fn() });
  });

  afterEach(() => {
    cleanup();
  });

  it("should render empty state when no spices are found", async () => {
    mockPartial(listSpices).mockResolvedValue({
      data: { spices: [] },
    });

    render(await SpiceTrackerPage());

    expect(screen.getByText("Spice Tracker")).toBeInTheDocument();
    expect(
      screen.getByText(
        "Your spice rack is empty. Start by adding some spices!",
      ),
    ).toBeInTheDocument();
  });

  it("should render list of spices when found", async () => {
    mockPartial(listSpices).mockResolvedValue({
      data: {
        spices: [
          { id: "1", name: "Cinnamon", quantity: "100g" },
          { id: "2", name: "Paprika", quantity: "50g" },
        ],
      },
    });

    render(await SpiceTrackerPage());

    expect(screen.getByText("Cinnamon")).toBeInTheDocument();
    expect(screen.getByText("100g")).toBeInTheDocument();
    expect(screen.getByText("Paprika")).toBeInTheDocument();
    expect(screen.getByText("50g")).toBeInTheDocument();
  });

  it("should show empty state and log error if listSpices fails", async () => {
    vi.mocked(listSpices).mockRejectedValue(new Error("API Error"));

    render(await SpiceTrackerPage());

    expect(
      screen.getByText(
        "Your spice rack is empty. Start by adding some spices!",
      ),
    ).toBeInTheDocument();
    expect(console.error).toHaveBeenCalledWith(
      "Failed to fetch spices:",
      expect.any(Error),
    );
  });
});
