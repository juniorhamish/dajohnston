"use server";

import { revalidatePath } from "next/cache";
import type { UpdateUserProfileRequest } from "@/generated";
import { updateCurrentUser } from "@/generated";

export async function updateUserProfileAction(data: UpdateUserProfileRequest) {
  try {
    await updateCurrentUser({
      body: data,
    });
    revalidatePath("/");
  } catch (error) {
    console.error("Failed to update user profile:", error);
    throw error;
  }
}
