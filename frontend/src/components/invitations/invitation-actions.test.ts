import { revalidatePath } from "next/cache";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { acceptInvitation, declineInvitation, inviteUser } from "@/generated";
import {
  acceptInvitationAction,
  declineInvitationAction,
  inviteUserAction,
} from "./invitation-actions";

vi.mock("@/generated/sdk.gen", () => ({
  acceptInvitation: vi.fn(),
  declineInvitation: vi.fn(),
  inviteUser: vi.fn(),
}));

vi.mock("next/cache", () => ({
  revalidatePath: vi.fn(),
}));

describe("invitation-actions", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe("inviteUserAction", () => {
    it("should call inviteUser and revalidatePath", async () => {
      const formData = new FormData();
      formData.append("email", "test@example.com");
      formData.append("role", "MEMBER");

      await inviteUserAction("h1", formData);

      expect(inviteUser).toHaveBeenCalledWith({
        path: { householdId: "h1" },
        body: { email: "test@example.com", role: "MEMBER" },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/");
    });
  });

  describe("acceptInvitationAction", () => {
    it("should call acceptInvitation and revalidatePath", async () => {
      await acceptInvitationAction("inv-1");

      expect(acceptInvitation).toHaveBeenCalledWith({
        path: { invitationId: "inv-1" },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/");
    });
  });

  describe("declineInvitationAction", () => {
    it("should call declineInvitation and revalidatePath", async () => {
      await declineInvitationAction("inv-1");

      expect(declineInvitation).toHaveBeenCalledWith({
        path: { invitationId: "inv-1" },
      });
      expect(revalidatePath).toHaveBeenCalledWith("/");
    });
  });
});
