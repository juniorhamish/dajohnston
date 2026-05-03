import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { JarQuantitySlider } from "./JarQuantitySlider";

describe("JarQuantitySlider", () => {
  it("should render with correct percentage", () => {
    const { getByText } = render(
      <JarQuantitySlider value={75} onChange={() => {}} />,
    );
    expect(getByText("75%")).toBeInTheDocument();
  });

  it("should call onChange when clicked", () => {
    const onChange = vi.fn();
    const { container } = render(
      <JarQuantitySlider value={50} onChange={onChange} />,
    );

    const jar = container.firstChild as HTMLElement;

    // Mock getBoundingClientRect
    jar.getBoundingClientRect = vi.fn(
      () =>
        ({
          width: 80,
          height: 128,
          top: 0,
          left: 0,
          bottom: 128,
          right: 80,
          x: 0,
          y: 0,
          toJSON: () => {},
        }) as DOMRect,
    );

    // Click at the top (should be 100%)
    fireEvent.mouseDown(jar, { clientY: 0 });
    fireEvent.mouseUp(globalThis);
    expect(onChange).toHaveBeenCalledWith(100);

    // Click at the bottom (should be 0%)
    fireEvent.mouseDown(jar, { clientY: 128 });
    fireEvent.mouseUp(globalThis);
    expect(onChange).toHaveBeenCalledWith(0);

    // Click in the middle (should be 50%)
    fireEvent.mouseDown(jar, { clientY: 64 });
    fireEvent.mouseUp(globalThis);
    expect(onChange).toHaveBeenCalledWith(50);
  });

  it("should not jump back to old value while waiting for prop update", async () => {
    const React = await import("react");
    const TestWrapper = () => {
      const [val, setVal] = React.useState(50);
      const handleChange = (newVal: number) => {
        // Simulate a delay in the parent update (e.g. server action)
        setTimeout(() => setVal(newVal), 50);
      };
      return <JarQuantitySlider value={val} onChange={handleChange} />;
    };

    const { getByText, container } = render(<TestWrapper />);
    const jar = container.firstChild as HTMLElement;
    jar.getBoundingClientRect = vi.fn(
      () =>
        ({
          width: 80,
          height: 128,
          top: 0,
          left: 0,
        }) as DOMRect,
    );

    // Drag to 100%
    fireEvent.mouseDown(jar, { clientY: 0 });
    expect(getByText("100%")).toBeInTheDocument();

    // Release - isDragging becomes false
    fireEvent.mouseUp(globalThis);

    // IMMEDIATELY after mouseUp, it should STILL be 100%
    // If it jumps back, it would show 50% here
    expect(getByText("100%")).toBeInTheDocument();

    // After the delay, it should still be 100% (now from the prop)
    await vi.waitFor(
      () => expect(screen.getByText("100%")).toBeInTheDocument(),
      {
        timeout: 200,
      },
    );
  });

  it("should revert to old value if the prop changes back (e.g. on failure)", async () => {
    const React = await import("react");
    const TestWrapper = () => {
      const [val, setVal] = React.useState(50);
      const handleChange = (newVal: number) => {
        // Simulate optimistic update followed by failure (revert)
        setVal(newVal);
        setTimeout(() => setVal(50), 50);
      };
      return <JarQuantitySlider value={val} onChange={handleChange} />;
    };

    const { getByText, container } = render(<TestWrapper />);
    const jar = container.firstChild as HTMLElement;
    jar.getBoundingClientRect = vi.fn(
      () =>
        ({
          width: 80,
          height: 128,
          top: 0,
          left: 0,
        }) as DOMRect,
    );

    fireEvent.mouseDown(jar, { clientY: 0 }); // 100%
    fireEvent.mouseUp(globalThis);

    expect(getByText("100%")).toBeInTheDocument();

    // After the failure (prop reverts to 50), the slider should also revert
    await vi.waitFor(
      () => expect(screen.getByText("50%")).toBeInTheDocument(),
      {
        timeout: 200,
      },
    );
  });

  it("should only call onChange once when dragging and then releasing", () => {
    const onChange = vi.fn();
    const { container } = render(
      <JarQuantitySlider value={50} onChange={onChange} />,
    );

    const jar = container.firstChild as HTMLElement;
    jar.getBoundingClientRect = vi.fn(
      () =>
        ({
          width: 80,
          height: 128,
          top: 0,
          left: 0,
        }) as DOMRect,
    );

    fireEvent.mouseDown(jar, { clientY: 64 }); // 50%
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.mouseMove(globalThis, { clientY: 32 }); // 75%
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.mouseMove(globalThis, { clientY: 0 }); // 100%
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.mouseUp(globalThis);
    expect(onChange).toHaveBeenCalledOnce();
    expect(onChange).toHaveBeenCalledWith(100);
  });

  it("should handle touch events correctly", () => {
    const onChange = vi.fn();
    const { container } = render(
      <JarQuantitySlider value={50} onChange={onChange} />,
    );

    const jar = container.firstChild as HTMLElement;
    jar.getBoundingClientRect = vi.fn(
      () =>
        ({
          width: 80,
          height: 128,
          top: 0,
          left: 0,
        }) as DOMRect,
    );

    fireEvent.touchStart(jar, { changedTouches: [{ clientY: 64 }] });
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.touchMove(globalThis, { changedTouches: [{ clientY: 0 }] });
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.touchEnd(globalThis);
    expect(onChange).toHaveBeenCalledWith(100);
  });

  it("should handle keyboard navigation", () => {
    const onChange = vi.fn();
    render(<JarQuantitySlider value={50} onChange={onChange} />);
    const slider = screen.getByRole("slider");

    // ArrowUp
    fireEvent.keyDown(slider, { key: "ArrowUp" });
    expect(onChange).toHaveBeenCalledWith(51);
    onChange.mockClear();

    // ArrowDown
    fireEvent.keyDown(slider, { key: "ArrowDown" });
    expect(onChange).toHaveBeenCalledWith(50);
    onChange.mockClear();

    // ArrowRight
    fireEvent.keyDown(slider, { key: "ArrowRight" });
    expect(onChange).toHaveBeenCalledWith(51);
    onChange.mockClear();

    // ArrowLeft
    fireEvent.keyDown(slider, { key: "ArrowLeft" });
    expect(onChange).toHaveBeenCalledWith(50);
    onChange.mockClear();

    // PageUp
    fireEvent.keyDown(slider, { key: "PageUp" });
    expect(onChange).toHaveBeenCalledWith(60);
    onChange.mockClear();

    // PageDown
    fireEvent.keyDown(slider, { key: "PageDown" });
    expect(onChange).toHaveBeenCalledWith(50);
    onChange.mockClear();

    // Home
    fireEvent.keyDown(slider, { key: "Home" });
    expect(onChange).toHaveBeenCalledWith(0);
    onChange.mockClear();

    // End
    fireEvent.keyDown(slider, { key: "End" });
    expect(onChange).toHaveBeenCalledWith(100);
    onChange.mockClear();

    // Other key
    fireEvent.keyDown(slider, { key: "Enter" });
    expect(onChange).not.toHaveBeenCalled();
  });

  it("should respect min/max during keyboard navigation", () => {
    const onChange = vi.fn();

    // Max limit
    const { unmount } = render(
      <JarQuantitySlider value={100} onChange={onChange} />,
    );
    fireEvent.keyDown(screen.getByRole("slider"), { key: "ArrowUp" });
    expect(onChange).toHaveBeenCalledWith(100);
    unmount();

    // Min limit
    onChange.mockClear();
    render(<JarQuantitySlider value={0} onChange={onChange} />);
    fireEvent.keyDown(screen.getByRole("slider"), { key: "ArrowDown" });
    expect(onChange).toHaveBeenCalledWith(0);
  });

  it("should not update internal value from props while dragging", () => {
    const { rerender } = render(
      <JarQuantitySlider value={50} onChange={vi.fn()} />,
    );
    const jar = screen.getByRole("slider");

    // Mock getBoundingClientRect
    jar.getBoundingClientRect = vi.fn(
      () =>
        ({
          width: 80,
          height: 128,
          top: 0,
          left: 0,
        }) as DOMRect,
    );

    // Start dragging
    fireEvent.mouseDown(jar, { clientY: 50 });

    // Prop changes while dragging
    rerender(<JarQuantitySlider value={100} onChange={vi.fn()} />);

    // Internal value should still be from drag, not prop
    expect(jar).toHaveAttribute("aria-valuenow", "61"); // Math.round((1 - 50/128) * 100) = 61
    fireEvent.mouseUp(globalThis);
  });

  it("should handle edge cases for coverage", () => {
    const onChange = vi.fn();
    render(<JarQuantitySlider value={50} onChange={onChange} />);

    // text-primary at exactly 50%
    expect(screen.getByText("50%")).toHaveClass("text-primary");
  });

  it("should ignore mouse/touch move when not dragging", () => {
    const onChange = vi.fn();
    render(<JarQuantitySlider value={50} onChange={onChange} />);

    fireEvent.mouseMove(globalThis, { clientY: 0 });
    expect(screen.getByRole("slider")).toHaveAttribute("aria-valuenow", "50");

    fireEvent.touchMove(globalThis, { touches: [{ clientY: 0 }] });
    expect(screen.getByRole("slider")).toHaveAttribute("aria-valuenow", "50");
  });

  it("should apply custom className", () => {
    render(
      <JarQuantitySlider
        value={50}
        onChange={vi.fn()}
        className="custom-class"
      />,
    );
    expect(screen.getByRole("slider")).toHaveClass("custom-class");
  });

  it("should change text color based on value", () => {
    const { rerender } = render(
      <JarQuantitySlider value={60} onChange={vi.fn()} />,
    );
    expect(screen.getByText("60%")).toHaveClass("text-primary-foreground");

    rerender(<JarQuantitySlider value={40} onChange={vi.fn()} />);
    expect(screen.getByText("40%")).toHaveClass("text-primary");
  });
});
