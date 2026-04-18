import { fireEvent, render, screen } from "@testing-library/react";
import { useTheme } from "next-themes";
import { describe, expect, it, vi } from "vitest";
import { mockPartial } from "@/lib/test-utils";
import { ThemeToggle } from "./theme-toggle";

vi.mock("next-themes", () => ({
  useTheme: vi.fn(),
}));

describe("ThemeToggle", () => {
  it("should toggle theme when clicked", () => {
    const setTheme = vi.fn();
    mockPartial(useTheme).mockReturnValue({
      theme: "light",
      setTheme,
    });

    render(<ThemeToggle />);

    const button = screen.getByRole("button");
    fireEvent.click(button);

    expect(setTheme).toHaveBeenCalledWith("dark");
  });

  it("should toggle to light theme when current is dark", () => {
    const setTheme = vi.fn();
    mockPartial(useTheme).mockReturnValue({
      theme: "dark",
      setTheme,
    });

    render(<ThemeToggle />);

    const button = screen.getByRole("button");
    fireEvent.click(button);

    expect(setTheme).toHaveBeenCalledWith("light");
  });
});
