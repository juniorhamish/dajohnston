import { revalidatePath } from "next/cache";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { deleteHousehold } from "@/generated";
import { deleteHouseholdAction } from "./household-actions";

vi.mock("@/generated/sdk.gen", () => ({
  deleteHousehold: vi.fn(),
}));

vi.mock("next/cache", () => ({
  revalidatePath: vi.fn(),
}));

describe("household-actions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
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
