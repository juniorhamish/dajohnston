import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import type { App } from "@/generated";
import { AppCard } from "./app-card";

afterEach(() => {
  cleanup();
});

describe("AppCard", () => {
  const baseApp: App = {
    id: "spice-tracker",
    name: "Spice Tracker",
    description: "Track your spice inventory",
    icon: "🌶️",
    url: "/apps/spice-tracker",
  };

  it("should render app name and description", () => {
    render(<AppCard app={baseApp} />);

    expect(
      screen.getByRole("heading", { name: "Spice Tracker" }),
    ).toBeInTheDocument();
    expect(screen.getByText("Track your spice inventory")).toBeInTheDocument();
  });

  it("should render a link to the app url", () => {
    render(<AppCard app={baseApp} />);

    const link = screen.getByRole("link");
    expect(link).toHaveAttribute("href", "/apps/spice-tracker");
  });

  it("should render the icon when provided", () => {
    render(<AppCard app={baseApp} />);

    expect(
      screen.getByRole("img", { name: "Spice Tracker icon" }),
    ).toBeInTheDocument();
  });

  it("should not render icon when not provided", () => {
    const appWithoutIcon: App = { id: "test", name: "Test", url: "/test" };
    render(<AppCard app={appWithoutIcon} />);

    expect(screen.queryByRole("img")).not.toBeInTheDocument();
  });

  it("should not render description when not provided", () => {
    const appWithoutDesc: App = { id: "test", name: "Test", url: "/test" };
    render(<AppCard app={appWithoutDesc} />);

    expect(screen.getByRole("heading", { name: "Test" })).toBeInTheDocument();
    expect(
      screen.queryByText("Track your spice inventory"),
    ).not.toBeInTheDocument();
  });
});
