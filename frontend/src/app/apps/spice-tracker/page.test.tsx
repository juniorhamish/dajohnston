import { cleanup, render, screen } from "@testing-library/react";
import { cookies } from "next/headers";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { listPantryJars, listSpices } from "@/generated/spice-tracker";
import { mockPartial } from "@/lib/test-utils";
import SpiceTrackerPage from "./page";

vi.mock("@/generated/spice-tracker", () => ({
  listSpices: vi.fn(),
  listPantryJars: vi.fn(),
}));

vi.mock("next/headers", () => ({
  cookies: vi.fn(),
}));

describe("SpiceTrackerPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("console", { ...console, error: vi.fn() });

    mockPartial(cookies).mockResolvedValue({
      get: () => ({ name: "", value: "test-household-id" }),
    });
  });

  afterEach(() => {
    cleanup();
  });

  it("should render no household selected state when cookie is missing", async () => {
    mockPartial(cookies).mockResolvedValue({
      get: () => undefined,
    });

    render(await SpiceTrackerPage());

    expect(screen.getByText("No Household Selected")).toBeInTheDocument();
  });

  it("should render SpiceTracker with fetched data", async () => {
    mockPartial(listSpices).mockResolvedValue({
      data: {
        spices: [{ id: "1", name: "Cumin" }],
      },
    });

    mockPartial(listPantryJars).mockResolvedValue({
      data: {
        jars: [{ id: "j1", spiceId: "1", spiceName: "Cumin", quantity: 50 }],
      },
    });

    render(await SpiceTrackerPage());

    expect(screen.getByText("Spice Tracker")).toBeInTheDocument();
    expect(screen.getByText("Cumin")).toBeInTheDocument();
    expect(screen.getByText("50% total")).toBeInTheDocument();
  });

  it("should handle error when fetching spices", async () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    mockPartial(listSpices).mockRejectedValue(new Error("API Error"));
    mockPartial(listPantryJars).mockResolvedValue({ data: { jars: [] } });

    render(await SpiceTrackerPage());

    expect(consoleSpy).toHaveBeenCalledWith(
      "Failed to fetch spices:",
      expect.any(Error),
    );
    expect(screen.getByText("Spice Tracker")).toBeInTheDocument();
  });

  it("should handle error when fetching pantry jars", async () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    mockPartial(listSpices).mockResolvedValue({ data: { spices: [] } });
    mockPartial(listPantryJars).mockRejectedValue(new Error("API Error"));

    render(await SpiceTrackerPage());

    expect(consoleSpy).toHaveBeenCalledWith(
      "Failed to fetch pantry jars:",
      expect.any(Error),
    );
    expect(screen.getByText("Spice Tracker")).toBeInTheDocument();
  });

  it("should handle missing data in responses", async () => {
    mockPartial(listSpices).mockResolvedValue({});
    mockPartial(listPantryJars).mockResolvedValue({ data: null });

    render(await SpiceTrackerPage());

    expect(screen.getByText("Spice Tracker")).toBeInTheDocument();
  });
});
