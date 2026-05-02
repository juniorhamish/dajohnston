import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";
import { describe, expect, it, vi } from "vitest";
import { mockPartial } from "@/lib/test-utils";
import { setActiveHousehold } from "./household";

vi.mock("next/headers", () => ({
  cookies: vi.fn(),
}));

vi.mock("next/cache", () => ({
  revalidatePath: vi.fn(),
}));

describe("household actions", () => {
  it("should set selected_household_id cookie and revalidate path", async () => {
    const set = vi.fn();
    mockPartial(cookies).mockResolvedValue({ set });

    await setActiveHousehold("h1");

    expect(set).toHaveBeenCalledWith("selected_household_id", "h1", {
      path: "/",
      sameSite: "lax",
      secure: false,
    });
    expect(revalidatePath).toHaveBeenCalledWith("/");
  });
});
