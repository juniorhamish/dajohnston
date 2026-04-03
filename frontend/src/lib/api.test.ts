import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { auth0 } from "@/lib/auth0";
import { mockSession } from "@/lib/test-utils";
import { apiFetch, getBackendUrl } from "./api";

vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn(),
  },
}));

describe("api.ts", () => {
  const originalApiUrl = process.env.NEXT_PUBLIC_API_URL;

  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("fetch", vi.fn());
    if (originalApiUrl === undefined) {
      delete process.env.NEXT_PUBLIC_API_URL;
    } else {
      process.env.NEXT_PUBLIC_API_URL = originalApiUrl;
    }
  });

  afterEach(() => {
    if (originalApiUrl === undefined) {
      delete process.env.NEXT_PUBLIC_API_URL;
    } else {
      process.env.NEXT_PUBLIC_API_URL = originalApiUrl;
    }
  });

  describe("getBackendUrl", () => {
    it("should return the environment variable if set", () => {
      process.env.NEXT_PUBLIC_API_URL = "https://api.example.com";
      expect(getBackendUrl()).toBe("https://api.example.com");
    });

    it("should return the default backend URL if environment variable is not set", () => {
      delete process.env.NEXT_PUBLIC_API_URL;
      expect(getBackendUrl()).toBe("http://localhost:8080");
    });

    it("should remove the trailing slash from the backend URL", () => {
      process.env.NEXT_PUBLIC_API_URL = "https://api.example.com/";
      expect(getBackendUrl()).toBe("https://api.example.com");
    });
  });

  describe("apiFetch", () => {
    it("should use a path starting with / as provided", async () => {
      vi.mocked(auth0.getSession).mockResolvedValue(null);
      vi.mocked(fetch).mockResolvedValue({ ok: true } as Response);

      await apiFetch("/my-path");

      expect(fetch).toHaveBeenCalledWith(
        "http://localhost:8080/my-path",
        expect.any(Object),
      );
    });

    it("should prepend a / if the path does not start with /", async () => {
      vi.mocked(auth0.getSession).mockResolvedValue(null);
      vi.mocked(fetch).mockResolvedValue({ ok: true } as Response);

      await apiFetch("no-slash-path");

      expect(fetch).toHaveBeenCalledWith(
        "http://localhost:8080/no-slash-path",
        expect.any(Object),
      );
    });

    it("should add Authorization header if token exists", async () => {
      vi.mocked(auth0.getSession).mockResolvedValue(
        mockSession({
          tokenSet: { accessToken: "my-token" },
        }),
      );
      vi.mocked(fetch).mockResolvedValue({ ok: true } as Response);

      await apiFetch("/test");

      expect(fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          headers: expect.any(Headers),
        }),
      );

      const headers = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers;
      expect(headers.get("Authorization")).toBe("Bearer my-token");
    });

    it("should not overwrite existing Authorization header", async () => {
      vi.mocked(auth0.getSession).mockResolvedValue(
        mockSession({
          tokenSet: { accessToken: "my-token" },
        }),
      );
      vi.mocked(fetch).mockResolvedValue({ ok: true } as Response);

      await apiFetch("/test", {
        headers: { Authorization: "Existing-Header" },
      });

      const headers = vi.mocked(fetch).mock.calls[0][1]?.headers as Headers;
      expect(headers.get("Authorization")).toBe("Existing-Header");
    });

    it("should pass other fetch options correctly", async () => {
      vi.mocked(auth0.getSession).mockResolvedValue(null);
      vi.mocked(fetch).mockResolvedValue({ ok: true } as Response);

      await apiFetch("/test", {
        method: "POST",
        body: JSON.stringify({ key: "value" }),
      });

      expect(fetch).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({
          method: "POST",
          body: JSON.stringify({ key: "value" }),
        }),
      );
    });
  });
});
