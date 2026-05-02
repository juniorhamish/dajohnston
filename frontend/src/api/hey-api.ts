import "server-only";

import { cookies } from "next/headers";
import type { CreateClientConfig } from "@/generated/client";
import { auth0 } from "@/lib/auth0";

const DEFAULT_BACKEND_URL = "http://localhost:8080";

export const createClientConfig: CreateClientConfig = (config) => ({
  ...config,
  baseUrl: process.env.NEXT_PUBLIC_API_URL || DEFAULT_BACKEND_URL,
  fetch: async (url, init) => {
    const cookieStore = await cookies();
    const householdId = cookieStore.get("selected_household_id")?.value;
    const headers = new Headers(init?.headers);
    if (householdId) {
      headers.set("X-Household-Id", householdId);
    }
    return fetch(url, { ...init, headers });
  },
  auth: async () => {
    const session = await auth0.getSession();
    return session?.tokenSet?.accessToken;
  },
});
