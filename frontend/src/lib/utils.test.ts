import { describe, expect, it } from "vitest";
import { cn } from "./utils";

describe("cn", () => {
  it("should merge classes correctly", () => {
    expect(cn("px-2", "py-2")).toBe("px-2 py-2");
  });

  it("should handle conditional classes", () => {
    expect(cn("px-2", false)).toBe("px-2");
    expect(cn("px-2", "py-2")).toBe("px-2 py-2");
  });

  it("should merge tailwind classes correctly", () => {
    expect(cn("px-2 p-4")).toBe("p-4");
  });
});
