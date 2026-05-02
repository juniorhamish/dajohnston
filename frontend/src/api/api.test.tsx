import { beforeEach, describe, expect, it, vi } from "vitest";
import { auth0 } from "@/lib/auth0";
import { mockPartial } from "@/lib/test-utils";

vi.mock("next/headers", () => ({
  cookies: vi.fn().mockResolvedValue({
    get: vi.fn(),
  }),
}));

vi.mock("server-only", () => ({}));
vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn(),
  },
}));

describe("api config", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
    mockPartial(fetch).mockResolvedValue({
      ok: true,
      text: async () => "",
      headers: { get: () => "application/json" },
    });
    vi.clearAllMocks();
    vi.resetModules();
    vi.unstubAllEnvs();
  });
  it("should use default API URL if NEXT_PUBLIC_API_URL is not set", async () => {
    delete process.env.NEXT_PUBLIC_API_URL;

    const { getApplicationInfo } = await import("@/generated/sdk.gen");
    await getApplicationInfo();

    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/actuator/info",
      expect.anything(),
    );
  });
  it("should use the API url from NEXT_PUBLIC_API_URL", async () => {
    process.env.NEXT_PUBLIC_API_URL = "https://remote-server.com/api";

    const { getApplicationInfo } = await import("@/generated/sdk.gen");
    await getApplicationInfo();

    expect(fetch).toHaveBeenCalledWith(
      "https://remote-server.com/api/actuator/info",
      expect.anything(),
    );
  });
  it("should set the auth header", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({
      tokenSet: { accessToken: "test-token-abc" },
    });

    const { getApplicationInfo } = await import("@/generated/sdk.gen");
    await getApplicationInfo();

    const callHeaders = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers;
    expect(callHeaders.get("Authorization")).toBe("Bearer test-token-abc");
  });

  it("should include X-Household-Id header if cookie is present", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({
      tokenSet: { accessToken: "test-token" },
    });
    const { cookies } = await import("next/headers");
    mockPartial(cookies).mockResolvedValue({
      get: vi.fn().mockReturnValue({ value: "h1" }),
    });

    const { getApplicationInfo } = await import("@/generated/sdk.gen");
    await getApplicationInfo();

    const callHeaders = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers;
    expect(callHeaders.get("X-Household-Id")).toBe("h1");
  });
});
