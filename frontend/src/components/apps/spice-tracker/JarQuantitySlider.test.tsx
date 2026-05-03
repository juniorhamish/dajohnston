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
    fireEvent.mouseUp(window);
    expect(onChange).toHaveBeenCalledWith(100);

    // Click at the bottom (should be 0%)
    fireEvent.mouseDown(jar, { clientY: 128 });
    fireEvent.mouseUp(window);
    expect(onChange).toHaveBeenCalledWith(0);

    // Click in the middle (should be 50%)
    fireEvent.mouseDown(jar, { clientY: 64 });
    fireEvent.mouseUp(window);
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
    fireEvent.mouseUp(window);

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
    fireEvent.mouseUp(window);

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

    fireEvent.mouseMove(window, { clientY: 32 }); // 75%
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.mouseMove(window, { clientY: 0 }); // 100%
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.mouseUp(window);
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

    fireEvent.touchStart(jar, { touches: [{ clientY: 64 }] });
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.touchMove(window, { touches: [{ clientY: 0 }] });
    expect(onChange).not.toHaveBeenCalled();

    fireEvent.touchEnd(window);
    expect(onChange).toHaveBeenCalledWith(100);
  });
});
