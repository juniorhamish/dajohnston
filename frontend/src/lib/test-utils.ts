import { vi } from "vitest";

export const mockPartial = <T>(obj: T) =>
  vi.mocked(obj, { partial: true, deep: true });
