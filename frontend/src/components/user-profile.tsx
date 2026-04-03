import Image from "next/image";
import { apiFetch } from "@/lib/api";
import { auth0 } from "@/lib/auth0";

interface Household {
  id: string;
  name: string;
  role: string;
}

interface UserMapping {
  id: string;
  households: Household[];
}

export async function UserProfile() {
  const session = await auth0.getSession();
  const user = session?.user;

  if (!user) {
    return null;
  }

  let mapping: UserMapping | null = null;

  try {
    const response = await apiFetch("/api/users/me");
    mapping = await response.json();
  } catch (e) {
    console.error("Failed to fetch user mapping:", e);
  }

  return (
    <div className="flex flex-col gap-6 p-8 bg-white rounded-xl shadow-lg border border-gray-100 w-full">
      <div className="flex items-center gap-6">
        {user.picture && (
          <Image
            width={80}
            height={80}
            src={user.picture}
            alt={user.name ?? "User Profile Picture"}
            className="w-20 h-20 rounded-full shadow-md border-2 border-primary/10"
          />
        )}
        <div className="flex-1">
          <h2 className="text-2xl font-bold text-gray-900 leading-tight">
            {user.name}
          </h2>
          <p className="text-gray-500 font-medium">{user.email}</p>
          {mapping?.id && (
            <p className="text-xs text-gray-400 mt-1 font-mono uppercase tracking-wider">
              ID: {mapping?.id}
            </p>
          )}
        </div>
      </div>

      <div className="pt-6 border-t border-gray-100">
        <h3 className="text-sm font-bold text-gray-900 uppercase tracking-widest mb-4">
          Household & Tenant Mapping
        </h3>

        {mapping?.households && mapping?.households.length > 0 ? (
          <div className="grid gap-3">
            {mapping?.households.map((h) => (
              <div
                key={h.id}
                className="flex items-center justify-between bg-blue-50/50 px-4 py-3 rounded-lg border border-blue-100/50"
              >
                <span className="font-semibold text-blue-900">{h.name}</span>
                <span className="text-xs font-bold uppercase px-2 py-1 bg-blue-100 text-blue-700 rounded-md">
                  {h.role}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="bg-orange-50/50 px-4 py-3 rounded-lg border border-orange-100/50 text-orange-800 text-sm font-medium">
            No household assigned. Contact your administrator or create one.
          </div>
        )}
      </div>
    </div>
  );
}
