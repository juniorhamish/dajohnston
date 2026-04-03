import { auth0 } from "@/lib/auth0";

const DEFAULT_BACKEND_URL = "http://localhost:8080";

export function getBackendUrl(): string {
  const url = process.env.NEXT_PUBLIC_API_URL || DEFAULT_BACKEND_URL;
  return url.replace(/\/$/, "");
}

export async function apiFetch(
  path: string,
  options: RequestInit = {},
): Promise<Response> {
  const session = await auth0.getSession();
  const token = session?.tokenSet?.accessToken;

  const headers = new Headers(options.headers);
  if (token && !headers.has("Authorization")) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const baseUrl = getBackendUrl();
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const fullUrl = `${baseUrl}${normalizedPath}`;

  return fetch(fullUrl, {
    ...options,
    headers,
  });
}
