"use server";

import { revalidatePath } from "next/cache";
import { cookies } from "next/headers";

export async function setActiveHousehold(householdId: string) {
  const cookieStore = await cookies();
  cookieStore.set("selected_household_id", householdId, {
    path: "/",
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
  });
  revalidatePath("/");
}
