import type { UserEvent } from "@testing-library/user-event";
import { vi } from "vitest";

export const mockPartial = <T>(obj: T) =>
  vi.mocked(obj, { partial: true, deep: true });

export const mockSizeForComponent = (
  component: HTMLElement,
  width: number,
  height: number,
) => {
  // Mock getBoundingClientRect
  component.getBoundingClientRect = vi.fn(
    () =>
      ({
        width,
        height,
        top: 0,
        left: 0,
        bottom: height,
        right: width,
        x: 0,
        y: 0,
        toJSON: () => {},
      }) as DOMRect,
  );
};

export const clickAtPoint = async (
  element: HTMLElement,
  y: number,
  user: UserEvent,
  doNotRelease?: boolean,
) => {
  let keysModifier = "";
  if (doNotRelease) {
    keysModifier = ">";
  }
  await user.pointer({
    keys: `[${"MouseLeft"}${keysModifier}]`,
    target: element,
    coords: { x: 0, y },
  });
};
