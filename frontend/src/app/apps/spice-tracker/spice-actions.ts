"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";
import {
  addPantryJar,
  createSpice,
  removePantryJar,
  removeSpice,
  updatePantryJar,
} from "@/generated/spice-tracker";

async function getHouseholdId() {
  const cookieStore = await cookies();
  const householdId = cookieStore.get("selected_household_id")?.value;
  if (!householdId) {
    throw new Error("No household selected");
  }
  return householdId;
}

export async function createSpiceAction(name: string) {
  const householdId = await getHouseholdId();
  try {
    const result = await createSpice({
      headers: { "X-Household-Id": householdId },
      body: { name },
    });
    revalidatePath("/apps/spice-tracker");
    return result.data;
  } catch (error) {
    console.error("Failed to create spice:", error);
    if (error instanceof Error && "status" in error && error.status === 409) {
      throw new Error("Spice with this name already exists");
    }
    throw error;
  }
}

export async function removeSpiceAction(id: string) {
  const householdId = await getHouseholdId();
  try {
    await removeSpice({
      path: { id },
      headers: { "X-Household-Id": householdId },
    });
    revalidatePath("/apps/spice-tracker");
  } catch (error) {
    console.error("Failed to remove spice:", error);
    throw error;
  }
}

export async function addPantryJarAction(spiceId: string, quantity: number) {
  const householdId = await getHouseholdId();
  try {
    const result = await addPantryJar({
      headers: { "X-Household-Id": householdId },
      body: { spiceId, quantity },
    });
    revalidatePath("/apps/spice-tracker");
    return result.data;
  } catch (error) {
    console.error("Failed to add jar:", error);
    throw error;
  }
}

export async function updatePantryJarAction(id: string, quantity: number) {
  const householdId = await getHouseholdId();
  try {
    const result = await updatePantryJar({
      path: { id },
      headers: { "X-Household-Id": householdId },
      body: { quantity },
    });
    revalidatePath("/apps/spice-tracker");
    return result.data;
  } catch (error) {
    console.error("Failed to update jar:", error);
    throw error;
  }
}

export async function removePantryJarAction(id: string) {
  const householdId = await getHouseholdId();
  try {
    await removePantryJar({
      path: { id },
      headers: { "X-Household-Id": householdId },
    });
    revalidatePath("/apps/spice-tracker");
  } catch (error) {
    console.error("Failed to remove jar:", error);
    throw error;
  }
}
