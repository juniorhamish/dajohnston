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
});
