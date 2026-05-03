import { fireEvent, render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import SpiceTracker from "./SpiceTracker";

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
    fireEvent.mouseUp(window);

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
    vi.spyOn(window, "confirm").mockReturnValue(true);

    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const deleteButtons = screen.getAllByLabelText(/Delete/);
    fireEvent.click(deleteButtons[0]); // Delete Cumin

    expect(window.confirm).toHaveBeenCalled();
    expect(removeSpiceAction).toHaveBeenCalledWith("1");
  });

  it("should not call removeSpiceAction when delete button is clicked but not confirmed", async () => {
    const { removeSpiceAction } = await import("./spice-actions");
    vi.spyOn(window, "confirm").mockReturnValue(false);

    render(<SpiceTracker spices={mockSpices} jars={mockJars} />);

    const deleteButtons = screen.getAllByLabelText(/Delete/);
    fireEvent.click(deleteButtons[0]); // Delete Cumin

    expect(window.confirm).toHaveBeenCalled();
    expect(removeSpiceAction).not.toHaveBeenCalled();
  });
});
