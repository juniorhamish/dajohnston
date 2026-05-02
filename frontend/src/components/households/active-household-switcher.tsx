import { cookies } from "next/headers";
import { getCurrentUser } from "@/generated";
import { HouseholdSwitcher } from "./household-switcher";

export async function ActiveHouseholdSwitcher() {
  const cookieStore = await cookies();
  const activeHouseholdId = cookieStore.get("selected_household_id")?.value;

  const { data: user } = await getCurrentUser().catch(() => ({
    data: undefined,
  }));

  if (!user || user.households.length === 0) {
    return null;
  }

  return (
    <HouseholdSwitcher
      households={user.households}
      activeHouseholdId={activeHouseholdId}
    />
  );
}
