import { cookies } from "next/headers";
import { listPantryJars, listSpices } from "@/generated/spice-tracker";
import SpiceTracker from "./SpiceTracker";

export const dynamic = "force-dynamic";

export default async function SpiceTrackerPage() {
  const cookieStore = await cookies();
  const householdId = cookieStore.get("selected_household_id")?.value;

  if (!householdId) {
    return (
      <div className="flex flex-col items-center justify-center h-[50vh] gap-4">
        <h1 className="text-2xl font-bold">No Household Selected</h1>
        <p className="text-muted-foreground">
          Please select a household to use the Spice Tracker.
        </p>
      </div>
    );
  }

  // Fetch spices
  const spicesResponse = await listSpices({
    headers: { "X-Household-Id": householdId },
  }).catch((e) => {
    console.error("Failed to fetch spices:", e);
    return { data: { spices: [] } };
  });

  // Fetch pantry jars
  const pantryResponse = await listPantryJars({
    headers: { "X-Household-Id": householdId },
  }).catch((e) => {
    console.error("Failed to fetch pantry jars:", e);
    return { data: { jars: [] } };
  });

  const spices = spicesResponse.data?.spices ?? [];
  const jars = pantryResponse.data?.jars ?? [];

  return (
    <div className="flex flex-col gap-8 w-full max-w-5xl mx-auto py-8 px-4">
      <div className="flex flex-col gap-2">
        <h1 className="text-4xl font-bold text-primary tracking-tight">
          Spice Tracker
        </h1>
        <p className="text-lg text-muted-foreground">
          Keep track of your spice inventory and never run out.
        </p>
      </div>

      <SpiceTracker spices={spices} jars={jars} />
    </div>
  );
}
