import {listApps} from "@/generated";
import {AppCard} from "./app-card";

export async function AppGrid() {
  const {data} = await listApps().catch((e) => {
    console.error("Failed to fetch apps:", e);
    return {data: undefined};
  });

  const apps = data?.apps ?? [];

  if (apps.length === 0) {
    return (
        <div className="w-full p-6 bg-gray-50 rounded-xl border border-gray-100 text-center">
          <p className="text-gray-500 text-sm font-medium">
            No applications available yet.
          </p>
        </div>
    );
  }

  return (
      <div className="w-full">
        <h2 className="text-xl font-bold text-gray-900 mb-4">
          Available Applications
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {apps.map((app) => (
              <AppCard key={app.id} app={app}/>
          ))}
        </div>
      </div>
  );
}
