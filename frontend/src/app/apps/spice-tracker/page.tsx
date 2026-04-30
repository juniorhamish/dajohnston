import { Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import { listSpices } from "@/generated/spice-tracker";

export const dynamic = "force-dynamic";

export default async function SpiceTrackerPage() {
  const { data } = await listSpices().catch((e) => {
    console.error("Failed to fetch spices:", e);
    return { data: undefined };
  });

  const spices = data?.spices ?? [];

  return (
    <div className="flex flex-col gap-8 items-center sm:items-start w-full max-w-4xl mx-auto">
      <div className="flex justify-between items-center w-full">
        <h1 className="text-4xl font-bold text-primary">Spice Tracker</h1>
        <Button>
          <Plus className="mr-2 h-4 w-4" /> Add Spice
        </Button>
      </div>

      <p className="text-lg text-center sm:text-left text-muted-foreground">
        Manage your household spices and inventory.
      </p>

      {spices.length === 0 ? (
        <div className="w-full p-12 bg-card rounded-xl border border-border text-center">
          <p className="text-muted-foreground text-sm font-medium">
            Your spice rack is empty. Start by adding some spices!
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 w-full">
          {spices.map((spice) => (
            <div
              key={spice.id}
              className="p-4 bg-card rounded-lg border border-border"
            >
              <h3 className="font-bold">{spice.name}</h3>
              <p className="text-sm text-muted-foreground">{spice.quantity}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
