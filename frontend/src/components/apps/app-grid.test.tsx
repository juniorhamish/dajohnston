import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { listApps } from "@/generated";
import { AppGrid } from "./app-grid";

vi.mock("@/generated", () => ({
  listApps: vi.fn(),
}));

afterEach(() => {
  cleanup();
});

describe("AppGrid", () => {
  it("should render apps when available", async () => {
    vi.mocked(listApps).mockResolvedValue({
      data: {
        apps: [
          {
            id: "spice-tracker",
            name: "Spice Tracker",
            description: "Track your spice inventory",
            icon: "🌶️",
            url: "/apps/spice-tracker",
          },
          {
            id: "meal-planner",
            name: "Meal Planner",
            description: "Plan your weekly meals",
            url: "/apps/meal-planner",
          },
        ],
      },
    } as Awaited<ReturnType<typeof listApps>>);

    render(await AppGrid());

    expect(
      screen.getByRole("heading", { name: "Available Applications" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "Spice Tracker" }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "Meal Planner" }),
    ).toBeInTheDocument();
    expect(screen.getAllByRole("link")).toHaveLength(2);
  });

  it("should render empty state when no apps available", async () => {
    vi.mocked(listApps).mockResolvedValue({
      data: { apps: [] },
    } as Awaited<ReturnType<typeof listApps>>);

    render(await AppGrid());

    expect(
      screen.getByText("No applications available yet."),
    ).toBeInTheDocument();
  });

  it("should render empty state when API call fails", async () => {
    vi.mocked(listApps).mockRejectedValue(new Error("Network error"));

    render(await AppGrid());

    expect(
      screen.getByText("No applications available yet."),
    ).toBeInTheDocument();
  });
});
