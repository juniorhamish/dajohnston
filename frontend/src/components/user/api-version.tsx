import { apiFetch } from "@/lib/api";

export async function ApiVersion() {
  try {
    const res = await apiFetch("/actuator/info", {
      next: { revalidate: 60 },
    });
    if (!res.ok) {
      return null;
    }
    const data = await res.json();
    const version = data.build?.version;

    if (!version) return null;

    return <p className="text-sm text-gray-500">API Version: {version}</p>;
  } catch (error) {
    console.error("Error fetching API version:", error);
    return null;
  }
}
