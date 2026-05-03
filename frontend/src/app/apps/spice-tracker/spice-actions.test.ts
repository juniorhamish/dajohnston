import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";
import { beforeEach, describe, expect, it, vi } from "vitest";
import {
  addPantryJar,
  createSpice,
  removePantryJar,
  removeSpice,
  updatePantryJar,
} from "@/generated/spice-tracker";
import {
  addPantryJarAction,
  createSpiceAction,
  removePantryJarAction,
  removeSpiceAction,
  updatePantryJarAction,
} from "./spice-actions";

vi.mock("next/cache", () => ({
  revalidatePath: vi.fn(),
}));

vi.mock("next/headers", () => ({
  cookies: vi.fn(),
}));

vi.mock("@/generated/spice-tracker", () => ({
  addPantryJar: vi.fn(),
  createSpice: vi.fn(),
  removePantryJar: vi.fn(),
  removeSpice: vi.fn(),
  updatePantryJar: vi.fn(),
}));

describe("spice-actions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const mockHouseholdId = "test-household-id";

  const setupCookies = (id: string | undefined) => {
    vi.mocked(cookies).mockResolvedValue({
      get: vi.fn().mockReturnValue(id ? { value: id } : undefined),
    } as unknown as Awaited<ReturnType<typeof cookies>>);
  };

  describe("getHouseholdId", () => {
    it("should throw error if no household is selected", async () => {
      setupCookies(undefined);
      await expect(createSpiceAction("Cumin")).rejects.toThrow(
        "No household selected",
      );
    });
  });

  describe("createSpiceAction", () => {
    it("should create a spice successfully", async () => {
      setupCookies(mockHouseholdId);
      vi.mocked(createSpice).mockResolvedValue({
        data: { id: "1", name: "Cumin" },
      });

      const result = await createSpiceAction("Cumin");

      expect(createSpice).toHaveBeenCalledWith({
        headers: { "X-Household-Id": mockHouseholdId },
        body: { name: "Cumin" },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/apps/spice-tracker");
      expect(result).toEqual({ id: "1", name: "Cumin" });
    });

    it("should throw specific error if spice already exists (409)", async () => {
      setupCookies(mockHouseholdId);
      const error = new Error("Conflict") as Error & { status: number };
      error.status = 409;
      vi.mocked(createSpice).mockRejectedValue(error);

      await expect(createSpiceAction("Cumin")).rejects.toThrow(
        "Spice with this name already exists",
      );
    });

    it("should log and rethrow other errors", async () => {
      setupCookies(mockHouseholdId);
      const error = new Error("API Error");
      vi.mocked(createSpice).mockRejectedValue(error);
      const consoleSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      await expect(createSpiceAction("Cumin")).rejects.toThrow("API Error");
      expect(consoleSpy).toHaveBeenCalledWith("Failed to create spice:", error);
    });
  });

  describe("removeSpiceAction", () => {
    it("should remove a spice successfully", async () => {
      setupCookies(mockHouseholdId);
      vi.mocked(removeSpice).mockResolvedValue({});

      await removeSpiceAction("1");

      expect(removeSpice).toHaveBeenCalledWith({
        path: { id: "1" },
        headers: { "X-Household-Id": mockHouseholdId },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/apps/spice-tracker");
    });

    it("should log and rethrow errors", async () => {
      setupCookies(mockHouseholdId);
      const error = new Error("API Error");
      vi.mocked(removeSpice).mockRejectedValue(error);
      const consoleSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      await expect(removeSpiceAction("1")).rejects.toThrow("API Error");
      expect(consoleSpy).toHaveBeenCalledWith("Failed to remove spice:", error);
    });
  });

  describe("addPantryJarAction", () => {
    it("should add a jar successfully", async () => {
      setupCookies(mockHouseholdId);
      vi.mocked(addPantryJar).mockResolvedValue({ data: { id: "j1" } });

      await addPantryJarAction("1", 50);

      expect(addPantryJar).toHaveBeenCalledWith({
        headers: { "X-Household-Id": mockHouseholdId },
        body: { spiceId: "1", quantity: 50 },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/apps/spice-tracker");
    });

    it("should log and rethrow errors", async () => {
      setupCookies(mockHouseholdId);
      const error = new Error("API Error");
      vi.mocked(addPantryJar).mockRejectedValue(error);
      const consoleSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      await expect(addPantryJarAction("1", 50)).rejects.toThrow("API Error");
      expect(consoleSpy).toHaveBeenCalledWith("Failed to add jar:", error);
    });
  });

  describe("updatePantryJarAction", () => {
    it("should update a jar successfully", async () => {
      setupCookies(mockHouseholdId);
      vi.mocked(updatePantryJar).mockResolvedValue({
        data: { id: "j1", quantity: 60 },
      });

      await updatePantryJarAction("j1", 60);

      expect(updatePantryJar).toHaveBeenCalledWith({
        path: { id: "j1" },
        headers: { "X-Household-Id": mockHouseholdId },
        body: { quantity: 60 },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/apps/spice-tracker");
    });

    it("should log and rethrow errors", async () => {
      setupCookies(mockHouseholdId);
      const error = new Error("API Error");
      vi.mocked(updatePantryJar).mockRejectedValue(error);
      const consoleSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      await expect(updatePantryJarAction("j1", 60)).rejects.toThrow(
        "API Error",
      );
      expect(consoleSpy).toHaveBeenCalledWith("Failed to update jar:", error);
    });
  });

  describe("removePantryJarAction", () => {
    it("should remove a jar successfully", async () => {
      setupCookies(mockHouseholdId);
      vi.mocked(removePantryJar).mockResolvedValue({});

      await removePantryJarAction("j1");

      expect(removePantryJar).toHaveBeenCalledWith({
        path: { id: "j1" },
        headers: { "X-Household-Id": mockHouseholdId },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/apps/spice-tracker");
    });

    it("should log and rethrow errors", async () => {
      setupCookies(mockHouseholdId);
      const error = new Error("API Error");
      vi.mocked(removePantryJar).mockRejectedValue(error);
      const consoleSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      await expect(removePantryJarAction("j1")).rejects.toThrow("API Error");
      expect(consoleSpy).toHaveBeenCalledWith("Failed to remove jar:", error);
    });
  });
});
