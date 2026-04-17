"use server";

import { revalidatePath } from "next/cache";
import { acceptInvitation, declineInvitation, inviteUser } from "@/generated";

export async function inviteUserAction(
  householdId: string,
  formData: FormData,
) {
  const email = formData.get("email") as string;
  const role = formData.get("role") as "OWNER" | "MEMBER";

  await inviteUser({
    path: { householdId },
    body: { email, role },
  });
  revalidatePath("/");
}

export async function acceptInvitationAction(invitationId: string) {
  await acceptInvitation({ path: { invitationId } });
  revalidatePath("/");
}

export async function declineInvitationAction(invitationId: string) {
  await declineInvitation({ path: { invitationId } });
  revalidatePath("/");
}
