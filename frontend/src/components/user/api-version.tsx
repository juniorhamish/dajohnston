import { getApplicationInfo } from "@/generated";

export async function ApiVersion() {
  const { data } = await getApplicationInfo().catch((error) => {
    console.error("Error fetching API version:", error);
    return { data: undefined };
  });
  const version = data?.build?.version;
  if (!version) {
    return null;
  }

  return <p className="text-sm text-gray-500">API Version: {version}</p>;
}
