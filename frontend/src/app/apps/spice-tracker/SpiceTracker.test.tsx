import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { Spice } from "@/generated/spice-tracker";
import {
  clickAtPoint,
  mockPartial,
  mockSizeForComponent,
} from "@/lib/test-utils";
import SpiceTracker from "./SpiceTracker";
import {
  addPantryJarAction,
  createSpiceAction,
  removePantryJarAction,
  removeSpiceAction,
  updatePantryJarAction,
} from "./spice-actions";

// Mock server actions
vi.mock("./spice-actions", () => ({
  createSpiceAction: vi.fn(),
  addPantryJarAction: vi.fn(),
  updatePantryJarAction: vi.fn(),
  removePantryJarAction: vi.fn(),
  removeSpiceAction: vi.fn(),
}));

const mockSpices = [
  { id: "1", name: "Cumin" },
  { id: "2", name: "Paprika" },
];

const mockJars = [
  { id: "j1", spiceId: "1", spiceName: "Cumin", quantity: 30 },
  { id: "j2", spiceId: "1", spiceName: "Cumin", quantity: 40 },
  { id: "j3", spiceId: "2", spiceName: "Paprika", quantity: 15 },
];

describe("SpiceTracker", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  it("should display spices with their total quantities", () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    expect(screen.getByText("Cumin")).toBeInTheDocument();
    expect(screen.getByText("70% total")).toBeInTheDocument();

    expect(screen.getByText("Paprika")).toBeInTheDocument();
    expect(screen.getByText("15% total")).toBeInTheDocument();
  });

  it("should toggle expanded state when clicked", async () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const cuminCard = screen.getByText("Cumin");
    await userEvent.click(cuminCard);

    expect(screen.getByText("Jar (30%)")).toBeInTheDocument();
    expect(screen.getByText("Jar (40%)")).toBeInTheDocument();
  });

  it("should show Add Spice form when button is clicked", async () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    await userEvent.click(screen.getByText("Add Spice"));

    expect(screen.getByPlaceholderText("e.g. Cumin")).toBeInTheDocument();
  });

  it("should show Add Jar form when button is clicked", async () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    await userEvent.click(screen.getByText("Add Jar to Pantry"));

    expect(screen.getByLabelText("Select Spice")).toBeInTheDocument();
    expect(screen.getByLabelText("Initial Quantity (%)")).toBeInTheDocument();
  });

  it("should call updatePantryJarAction when a jar is updated", async () => {
    const user = userEvent.setup();
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    // Expand Cumin
    await user.click(screen.getByText("Cumin"));

    // Find the slider for the first jar (30%)
    const slider = screen.getAllByRole("slider")[0];
    if (!slider) throw new Error("Slider not found");
    mockSizeForComponent(slider, 80, 128);

    // Drag to 100% (top)
    await clickAtPoint(slider, 0, user);

    expect(updatePantryJarAction).toHaveBeenCalledWith("j1", 100);
  });

  it("should maintain stable jar order even if jars prop reorders", async () => {
    const initialJars = [
      { id: "j1", spiceId: "1", spiceName: "Cumin", quantity: 30 },
      { id: "j2", spiceId: "1", spiceName: "Cumin", quantity: 40 },
    ];

    const { rerender } = render(
      <SpiceTracker spices={mockSpices} jars={initialJars} />,
    );

    // Expand Cumin
    await userEvent.click(screen.getByText("Cumin"));

    // Check initial order
    let jarElements = screen.getAllByText(/Jar \(\d+%\)/);
    expect(jarElements[0]).toHaveTextContent("Jar (30%)");
    expect(jarElements[1]).toHaveTextContent("Jar (40%)");

    // Rerender with jars in different order (e.g. sorted by quantity DESC)
    const reorderedJars = [
      { id: "j2", spiceId: "1", spiceName: "Cumin", quantity: 40 },
      { id: "j1", spiceId: "1", spiceName: "Cumin", quantity: 30 },
    ];

    rerender(<SpiceTracker spices={mockSpices} jars={reorderedJars} />);

    // Check order again - if not explicitly sorted, it will have changed
    jarElements = screen.getAllByText(/Jar \(\d+%\)/);
    expect(jarElements[0]).toHaveTextContent("Jar (30%)");
    expect(jarElements[1]).toHaveTextContent("Jar (40%)");
  });

  it("should display spices in alphabetical order", () => {
    const unsortedSpices = [
      { id: "1", name: "Paprika" },
      { id: "2", name: "Cumin" },
    ];
    render(<SpiceTracker spices={unsortedSpices} jars={[]} />);

    const titles = screen.getAllByRole("heading", { level: 3 });
    expect(titles[0]).toHaveTextContent("Cumin");
    expect(titles[1]).toHaveTextContent("Paprika");
  });

  it("should call removeSpiceAction when delete button is clicked and confirmed", async () => {
    vi.spyOn(globalThis, "confirm").mockReturnValue(true);

    const user = userEvent.setup();
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const deleteButtons = screen.getAllByLabelText(/Delete/);
    await user.click(deleteButtons[0]); // Delete Cumin

    expect(globalThis.confirm).toHaveBeenCalled();
    expect(removeSpiceAction).toHaveBeenCalledWith("1");
  });

  it("should not call removeSpiceAction when delete button is clicked but not confirmed", async () => {
    vi.spyOn(globalThis, "confirm").mockReturnValue(false);

    const user = userEvent.setup();
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const deleteButtons = screen.getAllByLabelText(/Delete/);
    await user.click(deleteButtons[0]); // Delete Cumin

    expect(globalThis.confirm).toHaveBeenCalled();
    expect(removeSpiceAction).not.toHaveBeenCalled();
  });

  it("should handle error when creating a spice", async () => {
    mockPartial(createSpiceAction).mockRejectedValue(new Error("Spice exists"));
    const user = userEvent.setup();
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    await user.click(screen.getByText("Add Spice"));
    const input = screen.getByPlaceholderText("e.g. Cumin");
    await user.type(input, "Cumin");
    await user.click(screen.getByText("Create Spice"));

    expect(await screen.findByText("Spice exists")).toBeInTheDocument();
  });

  it("should handle error when adding a jar", async () => {
    mockPartial(addPantryJarAction).mockRejectedValue(
      new Error("Add jar failed"),
    );
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    await userEvent.click(screen.getByText("Add Jar to Pantry"));
    await userEvent.selectOptions(
      screen.getByLabelText("Select Spice"),
      "Cumin",
    );
    await userEvent.click(screen.getByText("Add Jar"));

    expect(await screen.findByText("Add jar failed")).toBeInTheDocument();
  });

  it("should cancel adding a spice", async () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const user = userEvent.setup();
    await user.click(screen.getByText("Add Spice"));
    expect(screen.getByText("Add New Spice")).toBeInTheDocument();

    await user.click(screen.getByText("Cancel"));
    expect(screen.queryByText("Add New Spice")).not.toBeInTheDocument();
  });

  it("should cancel adding a jar", async () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const user = userEvent.setup();
    await user.click(screen.getByText("Add Jar to Pantry"));
    expect(
      screen.getByText("Add Jar to Pantry", { selector: "h3" }),
    ).toBeInTheDocument();

    const cancelButton = screen.getAllByText("Cancel")[0]; // There might be two if both forms are open, but only one is rendered at a time in our test
    await user.click(cancelButton);
    expect(
      screen.queryByText("Add Jar to Pantry", { selector: "h3" }),
    ).not.toBeInTheDocument();
  });

  it("should handle error when updating a jar", async () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    mockPartial(updatePantryJarAction).mockRejectedValue(
      new Error("Update failed"),
    );
    const user = userEvent.setup();
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    // Expand Cumin
    await user.click(screen.getByText("Cumin"));

    const slider = screen.getAllByRole("slider")[0];
    await clickAtPoint(slider, 0, user);

    await vi.waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(expect.any(Error));
    });
  });

  it("should handle error when removing a spice", async () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );
    mockPartial(removeSpiceAction).mockRejectedValue(
      new Error("Remove spice failed"),
    );

    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    await userEvent.click(screen.getByLabelText("Delete Cumin"));

    await vi.waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(expect.any(Error));
    });
  });

  it("should remove a jar when delete button is clicked and confirmed", async () => {
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );
    const user = userEvent.setup();
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    // Expand Cumin
    await user.click(screen.getByText("Cumin"));
    await userEvent.click(screen.getByRole("button", { name: "Delete Jar 1" }));

    expect(confirm).toHaveBeenCalled();
    expect(removePantryJarAction).toHaveBeenCalledWith("j1");
  });

  it("should handle error when removing a jar", async () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );
    mockPartial(removePantryJarAction).mockRejectedValue(
      new Error("Remove jar failed"),
    );

    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    // Expand Cumin
    await userEvent.click(screen.getByText("Cumin"));
    await userEvent.click(screen.getByRole("button", { name: "Delete Jar 1" }));

    await vi.waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(expect.any(Error));
    });
  });

  it("should show loader when pending", async () => {
    let resolveAction!: (value: Spice) => void;
    const promise = new Promise<Spice>((resolve) => {
      resolveAction = resolve;
    });
    mockPartial(createSpiceAction).mockReturnValue(promise);

    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    await userEvent.click(screen.getByText("Add Spice"));
    await userEvent.type(screen.getByPlaceholderText("e.g. Cumin"), "Cumin");
    await userEvent.click(screen.getByText("Create Spice"));

    // Check for loader
    expect(
      screen.getByRole("button", { name: /Create Spice/i }),
    ).toBeDisabled();

    if (resolveAction) {
      resolveAction({ id: "2", name: "Cumin" });
    }
    await vi.waitFor(() => {
      expect(
        screen.queryByRole("button", { name: /Create Spice/i }),
      ).not.toBeInTheDocument();
    });
  });

  it("should display correct status colors", () => {
    const diverseJars = [
      { id: "j1", spiceId: "1", spiceName: "A-Low", quantity: 10 },
      { id: "j2", spiceId: "2", spiceName: "B-Medium", quantity: 30 },
      { id: "j3", spiceId: "3", spiceName: "C-High", quantity: 60 },
    ];
    const diverseSpices = [
      { id: "1", name: "A-Low" },
      { id: "2", name: "B-Medium" },
      { id: "3", name: "C-High" },
    ];

    render(<SpiceTracker spices={diverseSpices} jars={diverseJars} />);

    // Check colors
    const indicators = document.querySelectorAll(".w-3.h-3.rounded-full");
    expect(indicators[0]).toHaveClass("bg-destructive"); // A-Low
    expect(indicators[1]).toHaveClass("bg-yellow-500"); // B-Medium
    expect(indicators[2]).toHaveClass("bg-green-500"); // C-High
  });

  it("should show empty pantry message when a spice has no jars", async () => {
    render(<SpiceTracker spices={[{ id: "1", name: "Empty" }]} jars={[]} />);
    await userEvent.click(screen.getByText("Empty"));
    expect(screen.getByText("No jars in pantry")).toBeInTheDocument();
  });

  it("should successfully add a jar", async () => {
    mockPartial(addPantryJarAction).mockResolvedValue({ id: "j4" });
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    await userEvent.click(screen.getByText("Add Jar to Pantry"));
    await userEvent.selectOptions(
      screen.getByLabelText("Select Spice"),
      "Cumin",
    );
    await userEvent.type(
      screen.getByLabelText("Initial Quantity (%)"),
      "[Backspace][Backspace][Backspace]50",
    );
    await userEvent.click(screen.getByRole("button", { name: "Add Jar" }));

    await vi.waitFor(() => {
      expect(addPantryJarAction).toHaveBeenCalledWith("1", 50);
      expect(
        screen.queryByText("Add Jar to Pantry", { selector: "h3" }),
      ).not.toBeInTheDocument();
    });
  });

  it("should not remove a jar if not confirmed", async () => {
    vi.stubGlobal(
      "confirm",
      vi.fn(() => false),
    );
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    // Expand Cumin
    await userEvent.click(screen.getByText("Cumin"));
    await userEvent.click(screen.getByRole("button", { name: "Delete Jar 1" }));

    expect(confirm).toHaveBeenCalled();
    expect(removePantryJarAction).not.toHaveBeenCalled();
  });

  it("should handle non-Error throws in handleAddSpice", async () => {
    mockPartial(createSpiceAction).mockRejectedValue("String error");
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    await userEvent.click(screen.getByText("Add Spice"));
    await userEvent.type(screen.getByPlaceholderText("e.g. Cumin"), "Cumin");
    await userEvent.click(screen.getByRole("button", { name: "Create Spice" }));

    // Error message should NOT be displayed because it's not instanceof Error
    await vi.waitFor(() => {
      expect(screen.queryByText("String error")).not.toBeInTheDocument();
    });
  });

  it("should handle non-Error throws in handleAddJar", async () => {
    mockPartial(addPantryJarAction).mockRejectedValue("String error");
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    await userEvent.click(screen.getByText("Add Jar to Pantry"));
    await userEvent.selectOptions(
      screen.getByLabelText("Select Spice"),
      "Cumin",
    );
    await userEvent.click(screen.getByRole("button", { name: "Add Jar" }));

    await vi.waitFor(() => {
      expect(screen.queryByText("String error")).not.toBeInTheDocument();
    });
  });
});
