import { revalidatePath } from "next/cache";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { updateCurrentUser } from "@/generated";
import { mockPartial } from "@/lib/test-utils";
import { updateUserProfileAction } from "./user-actions";

vi.mock("@/generated", () => ({
  updateCurrentUser: vi.fn(),
}));

vi.mock("next/cache", () => ({
  revalidatePath: vi.fn(),
}));

describe("user-actions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("updateUserProfileAction calls updateCurrentUser and revalidatePath", async () => {
    const data = { givenName: "John" };
    mockPartial(updateCurrentUser).mockResolvedValue({ data: {} });

    await updateUserProfileAction(data);

    expect(updateCurrentUser).toHaveBeenCalledWith({
      body: data,
    });
    expect(revalidatePath).toHaveBeenCalledWith("/");
  });

  it("updateUserProfileAction throws and logs error on failure", async () => {
    const error = new Error("API failed");
    vi.mocked(updateCurrentUser).mockRejectedValue(error);
    const consoleSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    await expect(updateUserProfileAction({})).rejects.toThrow("API failed");
    expect(consoleSpy).toHaveBeenCalledWith(
      "Failed to update user profile:",
      error,
    );

    consoleSpy.mockRestore();
  });
});
