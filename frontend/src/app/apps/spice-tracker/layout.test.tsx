import { render, screen } from "@testing-library/react";
import type { ReactNode } from "react";
import { describe, expect, it, vi } from "vitest";
import SpiceTrackerLayout from "./layout";

vi.mock("@/components/layouts/app-layout", () => ({
  AppLayout: ({ children }: { children: ReactNode }) => (
    <div data-testid="app-layout">{children}</div>
  ),
}));

describe("SpiceTrackerLayout", () => {
  it("should render children within AppLayout and a container div", () => {
    render(
      <SpiceTrackerLayout>
        <div data-testid="child">Spice Tracker Child</div>
      </SpiceTrackerLayout>,
    );

    const child = screen.getByTestId("child");
    const container = child.closest(".spice-tracker");
    
    expect(container).toBeInTheDocument();
    expect(container).toHaveClass("spice-tracker", "min-h-screen");

    const appLayout = screen.getByTestId("app-layout");
    expect(appLayout).toBeInTheDocument();
    expect(appLayout).toContainElement(child);
  });
});
