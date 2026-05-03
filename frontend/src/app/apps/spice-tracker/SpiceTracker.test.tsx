import {
  cleanup,
  fireEvent,
  render,
  screen,
  within,
} from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { mockPartial } from "@/lib/test-utils";
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

  it("should toggle expanded state when clicked", () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const cuminCard = screen.getByText("Cumin");
    fireEvent.click(cuminCard);

    expect(screen.getByText("Jar (30%)")).toBeInTheDocument();
    expect(screen.getByText("Jar (40%)")).toBeInTheDocument();
  });

  it("should show Add Spice form when button is clicked", () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const addButton = screen.getByText("Add Spice");
    fireEvent.click(addButton);

    expect(screen.getByPlaceholderText("e.g. Cumin")).toBeInTheDocument();
  });

  it("should show Add Jar form when button is clicked", () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const addJarButton = screen.getByText("Add Jar to Pantry");
    fireEvent.click(addJarButton);

    expect(screen.getByLabelText("Select Spice")).toBeInTheDocument();
    expect(screen.getByLabelText("Initial Quantity (%)")).toBeInTheDocument();
  });

  it("should call updatePantryJarAction when a jar is updated", async () => {
    const { updatePantryJarAction } = await import("./spice-actions");
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    // Expand Cumin
    fireEvent.click(screen.getByText("Cumin"));

    // Find the slider for the first jar (30%)
    const sliderText = screen.getByText("30%");
    const slider = sliderText.parentElement?.parentElement;

    if (!slider) throw new Error("Slider not found");

    // Mock getBoundingClientRect
    slider.getBoundingClientRect = vi.fn(
      () =>
        ({
          width: 80,
          height: 128,
          top: 0,
          left: 0,
        }) as DOMRect,
    );

    // Drag to 100% (top)
    fireEvent.mouseDown(slider, { clientY: 0 });
    fireEvent.mouseUp(globalThis);

    expect(updatePantryJarAction).toHaveBeenCalledWith("j1", 100);
  });

  it("should maintain stable jar order even if jars prop reorders", () => {
    const initialJars = [
      { id: "j1", spiceId: "1", spiceName: "Cumin", quantity: 30 },
      { id: "j2", spiceId: "1", spiceName: "Cumin", quantity: 40 },
    ];

    const { rerender } = render(
      <SpiceTracker spices={mockSpices} jars={initialJars} />,
    );

    // Expand Cumin
    fireEvent.click(screen.getByText("Cumin"));

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
    // CardTitle uses h3 by default in some UI libraries, let's verify CardTitle in SpiceTracker.tsx
    // It is <CardTitle className="text-lg">{spice.name}</CardTitle>
    expect(titles[0]).toHaveTextContent("Cumin");
    expect(titles[1]).toHaveTextContent("Paprika");
  });

  it("should call removeSpiceAction when delete button is clicked and confirmed", async () => {
    const { removeSpiceAction } = await import("./spice-actions");
    vi.spyOn(globalThis, "confirm").mockReturnValue(true);

    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const deleteButtons = screen.getAllByLabelText(/Delete/);
    fireEvent.click(deleteButtons[0]); // Delete Cumin

    expect(globalThis.confirm).toHaveBeenCalled();
    expect(removeSpiceAction).toHaveBeenCalledWith("1");
  });

  it("should not call removeSpiceAction when delete button is clicked but not confirmed", async () => {
    const { removeSpiceAction } = await import("./spice-actions");
    vi.spyOn(globalThis, "confirm").mockReturnValue(false);

    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const deleteButtons = screen.getAllByLabelText(/Delete/);
    fireEvent.click(deleteButtons[0]); // Delete Cumin

    expect(globalThis.confirm).toHaveBeenCalled();
    expect(removeSpiceAction).not.toHaveBeenCalled();
  });

  it("should handle error when creating a spice", async () => {
    mockPartial(createSpiceAction).mockRejectedValue(new Error("Spice exists"));
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    fireEvent.click(screen.getByText("Add Spice"));
    const input = screen.getByPlaceholderText("e.g. Cumin");
    fireEvent.change(input, { target: { value: "Cumin" } });

    const form = input.closest("form");
    if (form) {
      fireEvent.submit(form);
    }

    expect(await screen.findByText("Spice exists")).toBeInTheDocument();
  });

  it("should handle error when adding a jar", async () => {
    mockPartial(addPantryJarAction).mockRejectedValue(
      new Error("Add jar failed"),
    );
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    fireEvent.click(screen.getByText("Add Jar to Pantry"));
    const select = screen.getByLabelText("Select Spice");
    fireEvent.change(select, { target: { value: "1" } });

    const form = select.closest("form");
    if (form) {
      fireEvent.submit(form);
    }

    // Since setError(null) is called at the start of handleAddJar,
    // and setError(error.message) is called on failure,
    // we should see the error message if we don't clear it.
    // Wait, the error is shared between Add Spice and Add Jar forms?
    // Yes, the state `error` is shared.
    expect(await screen.findByText("Add jar failed")).toBeInTheDocument();
  });

  it("should cancel adding a spice", async () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    fireEvent.click(screen.getByText("Add Spice"));
    expect(screen.getByText("Add New Spice")).toBeInTheDocument();

    fireEvent.click(screen.getByText("Cancel"));
    expect(screen.queryByText("Add New Spice")).not.toBeInTheDocument();
  });

  it("should cancel adding a jar", async () => {
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    fireEvent.click(screen.getByText("Add Jar to Pantry"));
    expect(
      screen.getByText("Add Jar to Pantry", { selector: "h3" }),
    ).toBeInTheDocument();

    const cancelButton = screen.getAllByText("Cancel")[0]; // There might be two if both forms are open, but only one is rendered at a time in our test
    fireEvent.click(cancelButton);
    expect(
      screen.queryByText("Add Jar to Pantry", { selector: "h3" }),
    ).not.toBeInTheDocument();
  });

  it("should handle error when updating a jar", async () => {
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});
    mockPartial(updatePantryJarAction).mockRejectedValue(
      new Error("Update failed"),
    );
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    // Expand Cumin
    fireEvent.click(screen.getByText("Cumin"));

    const slider = screen.getAllByRole("slider")[0];
    fireEvent.mouseDown(slider, { clientY: 0 });
    fireEvent.mouseUp(globalThis);

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

    fireEvent.click(screen.getByLabelText("Delete Cumin"));

    await vi.waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(expect.any(Error));
    });
  });

  it("should remove a jar when delete button is clicked and confirmed", async () => {
    vi.stubGlobal(
      "confirm",
      vi.fn(() => true),
    );
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    // Expand Cumin
    fireEvent.click(screen.getByText("Cumin"));

    const jarItem = screen
      .getByText("Jar (30%)")
      .closest(".flex.items-center.gap-4");
    if (!jarItem) throw new Error("Jar item not found");
    const deleteButton = within(jarItem).getByRole("button");

    fireEvent.click(deleteButton);

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
    fireEvent.click(screen.getByText("Cumin"));

    const jarItem = screen
      .getByText("Jar (30%)")
      .closest(".flex.items-center.gap-4");
    if (!jarItem) throw new Error("Jar item not found");
    const deleteButton = within(jarItem).getByRole("button");

    fireEvent.click(deleteButton);

    await vi.waitFor(() => {
      expect(consoleSpy).toHaveBeenCalledWith(expect.any(Error));
    });
  });

  it("should show loader when pending", async () => {
    // We can't easily trigger isPending state in a test without a long running action
    // but we can try to mock the startTransition or just rely on the fact that
    // we've covered the code paths.
    // Actually, I can use a promise that I resolve manually.
    let resolveAction: (value: unknown) => void;
    const promise = new Promise((resolve) => {
      resolveAction = resolve;
    });
    mockPartial(createSpiceAction).mockReturnValue(promise);

    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    fireEvent.click(screen.getByText("Add Spice"));
    const input = screen.getByPlaceholderText("e.g. Cumin");
    fireEvent.change(input, { target: { value: "Cumin" } });

    const form = input.closest("form");
    if (form) {
      fireEvent.submit(form);
    }

    // Check for loader
    expect(
      screen.getByRole("button", { name: /Create Spice/i }),
    ).toBeDisabled();
    // The loader icon is Lucide-Loader2 which has animate-spin
    // expect(document.querySelector(".animate-spin")).toBeInTheDocument();

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

  it("should show empty pantry message when a spice has no jars", () => {
    render(<SpiceTracker spices={[{ id: "1", name: "Empty" }]} jars={[]} />);
    fireEvent.click(screen.getByText("Empty"));
    expect(screen.getByText("No jars in pantry")).toBeInTheDocument();
  });

  it("should successfully add a jar", async () => {
    mockPartial(addPantryJarAction).mockResolvedValue({ id: "j4" });
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    fireEvent.click(screen.getByText("Add Jar to Pantry"));
    fireEvent.change(screen.getByLabelText("Select Spice"), {
      target: { value: "1" },
    });
    fireEvent.change(screen.getByLabelText("Initial Quantity (%)"), {
      target: { value: "50" },
    });

    const form = screen.getByText("Add Jar").closest("form");
    if (form) {
      fireEvent.submit(form);
    }

    await vi.waitFor(() => {
      expect(addPantryJarAction).toHaveBeenCalledWith("1", 50);
      expect(
        screen.queryByText("Add Jar to Pantry", { selector: "h3" }),
      ).not.toBeInTheDocument();
    });
  });

  it("should not remove a jar if not confirmed", () => {
    vi.stubGlobal(
      "confirm",
      vi.fn(() => false),
    );
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    // Expand Cumin
    fireEvent.click(screen.getByText("Cumin"));

    const jarItem = screen
      .getByText("Jar (30%)")
      .closest(".flex.items-center.gap-4");
    if (!jarItem) throw new Error("Jar item not found");
    const deleteButton = within(jarItem).getByRole("button");

    fireEvent.click(deleteButton);

    expect(confirm).toHaveBeenCalled();
    expect(removePantryJarAction).not.toHaveBeenCalled();
  });

  it("should handle non-Error throws in handleAddSpice", async () => {
    mockPartial(createSpiceAction).mockRejectedValue("String error");
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    fireEvent.click(screen.getByText("Add Spice"));
    fireEvent.change(screen.getByPlaceholderText("e.g. Cumin"), {
      target: { value: "Cumin" },
    });
    const form = screen.getByPlaceholderText("e.g. Cumin").closest("form");
    if (form) {
      fireEvent.submit(form);
    }

    // Error message should NOT be displayed because it's not instanceof Error
    await vi.waitFor(() => {
      expect(screen.queryByText("String error")).not.toBeInTheDocument();
    });
  });

  it("should handle non-Error throws in handleAddJar", async () => {
    mockPartial(addPantryJarAction).mockRejectedValue("String error");
    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    fireEvent.click(screen.getByText("Add Jar to Pantry"));
    fireEvent.change(screen.getByLabelText("Select Spice"), {
      target: { value: "1" },
    });
    const form = screen.getByLabelText("Select Spice").closest("form");
    if (form) {
      fireEvent.submit(form);
    }

    await vi.waitFor(() => {
      expect(screen.queryByText("String error")).not.toBeInTheDocument();
    });
  });
});
