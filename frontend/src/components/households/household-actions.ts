"use server";

import { revalidatePath } from "next/cache";
import { createHousehold, deleteHousehold } from "@/generated";

export async function createHouseholdAction(name: string) {
  try {
    await createHousehold({
      body: { name },
    });
    revalidatePath("/");
  } catch (error) {
    console.error("Failed to create household:", error);
    throw error;
  }
}

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
