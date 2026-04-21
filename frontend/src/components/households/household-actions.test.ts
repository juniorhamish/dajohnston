import { revalidatePath } from "next/cache";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { createHousehold, deleteHousehold } from "@/generated";
import {
  createHouseholdAction,
  deleteHouseholdAction,
} from "./household-actions";

vi.mock("@/generated/sdk.gen", () => ({
  createHousehold: vi.fn(),
  deleteHousehold: vi.fn(),
}));

vi.mock("next/cache", () => ({
  revalidatePath: vi.fn(),
}));

describe("household-actions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("createHouseholdAction", () => {
    it("should call createHousehold and revalidatePath", async () => {
      await createHouseholdAction("My Home");

      expect(createHousehold).toHaveBeenCalledWith({
        body: { name: "My Home" },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/");
    });

    it("should log error and rethrow when createHousehold fails", async () => {
      const error = new Error("Create failed");
      vi.mocked(createHousehold).mockRejectedValueOnce(error);
      const consoleSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      await expect(createHouseholdAction("My Home")).rejects.toThrow(
        "Create failed",
      );

      expect(consoleSpy).toHaveBeenCalledWith(
        "Failed to create household:",
        error,
      );
      consoleSpy.mockRestore();
    });
  });

  describe("deleteHouseholdAction", () => {
    it("should call deleteHousehold and revalidatePath", async () => {
      await deleteHouseholdAction("h1");

      expect(deleteHousehold).toHaveBeenCalledWith({
        path: { householdId: "h1" },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/");
    });

    it("should log error and rethrow when deleteHousehold fails", async () => {
      const error = new Error("Delete failed");
      vi.mocked(deleteHousehold).mockRejectedValueOnce(error);
      const consoleSpy = vi
        .spyOn(console, "error")
        .mockImplementation(() => {});

      await expect(deleteHouseholdAction("h1")).rejects.toThrow(
        "Delete failed",
      );

      expect(consoleSpy).toHaveBeenCalledWith(
        "Failed to delete household:",
        error,
      );
      consoleSpy.mockRestore();
    });
  });
});
