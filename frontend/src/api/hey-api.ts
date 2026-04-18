import "server-only";

import type { CreateClientConfig } from "@/generated/client";
import { auth0 } from "@/lib/auth0";

const DEFAULT_BACKEND_URL = "http://localhost:8080";

export const createClientConfig: CreateClientConfig = (config) => ({
  ...config,
  baseUrl: process.env.NEXT_PUBLIC_API_URL || DEFAULT_BACKEND_URL,
  auth: async () => {
    const session = await auth0.getSession();
    return session?.tokenSet?.accessToken;
  },
});
