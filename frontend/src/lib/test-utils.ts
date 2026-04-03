import type { SessionData } from "@auth0/nextjs-auth0/types";

type SessionOverrides = Omit<SessionData, "user" | "tokenSet" | "internal"> & {
  user?: Partial<SessionData["user"]>;
  tokenSet?: Partial<SessionData["tokenSet"]>;
  internal?: Partial<SessionData["internal"]>;
};

export function mockSession(overrides?: SessionOverrides): SessionData {
  return {
    user: {
      sub: "test-sub",
      name: "Test User",
      email: "test@example.com",
      picture: "https://example.com/pic.jpg",
      ...overrides?.user,
    },
    tokenSet: {
      accessToken: "test-access-token",
      expiresAt: Math.floor(Date.now() / 1000) + 3600,
      ...overrides?.tokenSet,
    },
    internal: {
      sid: "test-sid",
      createdAt: Date.now(),
      ...overrides?.internal,
    },
  };
}
