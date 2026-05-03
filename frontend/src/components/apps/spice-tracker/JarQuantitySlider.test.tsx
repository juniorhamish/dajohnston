import { fireEvent, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { useState } from "react";
import { describe, expect, it, vi } from "vitest";
import { clickAtPoint, mockSizeForComponent } from "@/lib/test-utils";
import { JarQuantitySlider } from "./JarQuantitySlider";

describe("JarQuantitySlider", () => {
  it("should render with correct percentage", () => {
    const { getByText } = render(
      <JarQuantitySlider value={75} onChange={() => {}} />,
    );
    expect(getByText("75%")).toBeInTheDocument();
  });

  it("should call onChange when clicked", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<JarQuantitySlider value={50} onChange={onChange} />);

    const jar = screen.getByRole("slider");
    mockSizeForComponent(jar, 80, 128);

    // Click at the top (should be 100%)
    await clickAtPoint(jar, 0, user);
    expect(onChange).toHaveBeenCalledWith(100);
    // Click at the bottom (should be 0%)
    await clickAtPoint(jar, 128, user);
    expect(onChange).toHaveBeenCalledWith(0);
    // Click in the middle (should be 50%)
    await clickAtPoint(jar, 64, user);
    expect(onChange).toHaveBeenCalledWith(50);
  });

  it("should not jump back to old value while waiting for prop update", async () => {
    const user = userEvent.setup();
    const TestWrapper = () => {
      const [val, setVal] = useState(50);
      const handleChange = (newVal: number) => {
        // Simulate a delay in the parent update (e.g. server action)
        setTimeout(() => setVal(newVal), 50);
      };
      return <JarQuantitySlider value={val} onChange={handleChange} />;
    };

    render(<TestWrapper />);
    const jar = screen.getByRole("slider");
    mockSizeForComponent(jar, 80, 128);

    // Drag to 100%
    await clickAtPoint(jar, 0, user, true);
    expect(screen.getByText("100%")).toBeInTheDocument();
    // Release - isDragging becomes false
    await user.pointer({ keys: "[/MouseLeft]", target: jar });

    // IMMEDIATELY after mouseUp, it should STILL be 100%
    // If it jumps back, it would show 50% here
    expect(screen.getByText("100%")).toBeInTheDocument();

    // After the delay, it should still be 100% (now from the prop)
    await vi.waitFor(
      () => expect(screen.getByText("100%")).toBeInTheDocument(),
      {
        timeout: 200,
      },
    );
  });

  it("should revert to old value if the prop changes back (e.g. on failure)", async () => {
    const user = userEvent.setup();
    const TestWrapper = () => {
      const [val, setVal] = useState(50);
      const handleChange = (newVal: number) => {
        // Simulate optimistic update followed by failure (revert)
        setVal(newVal);
        setTimeout(() => setVal(50), 50);
      };
      return <JarQuantitySlider value={val} onChange={handleChange} />;
    };

    render(<TestWrapper />);
    const jar = screen.getByRole("slider");
    mockSizeForComponent(jar, 80, 128);

    await clickAtPoint(jar, 0, user);

    expect(screen.getByText("100%")).toBeInTheDocument();

    // After the failure (prop reverts to 50), the slider should also revert
    await vi.waitFor(
      () => expect(screen.getByText("50%")).toBeInTheDocument(),
      {
        timeout: 200,
      },
    );
  });

  it("should only call onChange once when dragging and then releasing", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<JarQuantitySlider value={50} onChange={onChange} />);

    const jar = screen.getByRole("slider");
    mockSizeForComponent(jar, 80, 128);

    await clickAtPoint(jar, 64, user, true); // 50%
    expect(onChange).not.toHaveBeenCalled();
    await user.pointer({ coords: { x: 0, y: 32 } }); // 75%
    expect(onChange).not.toHaveBeenCalled();
    await user.pointer({ coords: { x: 0, y: 0 } }); // 100%
    expect(onChange).not.toHaveBeenCalled();

    await user.pointer({ keys: "[/MouseLeft]", target: jar });
    expect(onChange).toHaveBeenCalledOnce();
    expect(onChange).toHaveBeenCalledWith(100);
  });

  it("should handle touch events correctly", () => {
    const onChange = vi.fn();
    render(<JarQuantitySlider value={50} onChange={onChange} />);

    const jar = screen.getByRole("slider");
    mockSizeForComponent(jar, 80, 128);

    fireEvent.touchStart(jar, { changedTouches: [{ clientY: 64 }] });
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.touchMove(jar, { changedTouches: [{ clientY: 0 }] });
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.touchEnd(jar);
    expect(onChange).toHaveBeenCalledWith(100);
  });

  it("should handle keyboard navigation", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();
    render(<JarQuantitySlider value={50} onChange={onChange} />);
    await user.tab();

    // ArrowUp
    await user.keyboard("[ArrowUp]");
    expect(onChange).toHaveBeenCalledWith(51);
    onChange.mockClear();

    // ArrowDown
    await user.keyboard("[ArrowDown]");
    expect(onChange).toHaveBeenCalledWith(50);
    onChange.mockClear();

    // ArrowRight
    await user.keyboard("[ArrowRight]");
    expect(onChange).toHaveBeenCalledWith(51);
    onChange.mockClear();

    // ArrowLeft
    await user.keyboard("[ArrowLeft]");
    expect(onChange).toHaveBeenCalledWith(50);
    onChange.mockClear();

    // PageUp
    await user.keyboard("[PageUp]");
    expect(onChange).toHaveBeenCalledWith(60);
    onChange.mockClear();

    // PageDown
    await user.keyboard("[PageDown]");
    expect(onChange).toHaveBeenCalledWith(50);
    onChange.mockClear();

    // Home
    await user.keyboard("[Home]");
    expect(onChange).toHaveBeenCalledWith(0);
    onChange.mockClear();

    // End
    await user.keyboard("[End]");
    expect(onChange).toHaveBeenCalledWith(100);
    onChange.mockClear();

    // Other key
    await user.keyboard("[Enter]");
    expect(onChange).not.toHaveBeenCalled();
  });

  it("should respect min during keyboard navigation", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();

    render(<JarQuantitySlider value={0} onChange={onChange} />);
    await user.tab();

    await user.keyboard("[ArrowDown]");
    expect(onChange).toHaveBeenCalledWith(0);
  });

  it("should respect max during keyboard navigation", async () => {
    const user = userEvent.setup();
    const onChange = vi.fn();

    render(<JarQuantitySlider value={100} onChange={onChange} />);
    await user.tab();

    await user.keyboard("[ArrowUp]");
    expect(onChange).toHaveBeenCalledWith(100);
  });

  it("should not update internal value from props while dragging", async () => {
    const user = userEvent.setup();
    const { rerender } = render(
      <JarQuantitySlider value={50} onChange={vi.fn()} />,
    );
    const jar = screen.getByRole("slider");
    mockSizeForComponent(jar, 80, 128);

    // Start dragging
    await clickAtPoint(jar, 50, user, true);

    // Prop changes while dragging
    rerender(<JarQuantitySlider value={100} onChange={vi.fn()} />);

    // Internal value should still be from drag, not prop
    expect(jar).toHaveAttribute("aria-valuenow", "61"); // Math.round((1 - 50/128) * 100) = 61
  });
});
