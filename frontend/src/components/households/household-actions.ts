"use server";

import { revalidatePath } from "next/cache";
import { deleteHousehold } from "@/generated";

export async function deleteHouseholdAction(householdId: string) {
  try {
    await deleteHousehold({
      path: { householdId },
    });
    revalidatePath("/");
  } catch (error) {
    console.error("Failed to delete household:", error);
    throw error;
  }
}
