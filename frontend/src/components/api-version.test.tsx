import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { ApiVersion } from "./api-version";

vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn().mockResolvedValue(null),
  },
}));

describe("ApiVersion", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
    vi.stubGlobal("console", { ...console, error: vi.fn() });
    vi.clearAllMocks();
  });

  it("should render the API version when the fetch is successful", async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ build: { version: "1.2.3" } }),
    } as Response);

    const component = await ApiVersion();
    render(component);

    expect(screen.getByText("API Version: 1.2.3")).toBeInTheDocument();
  });

  it("should return null when the fetch fails", async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: false,
    } as Response);

    const result = await ApiVersion();
    expect(result).toBeNull();
  });

  it("should return null when the version is missing from the data", async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ build: {} }),
    } as Response);

    const result = await ApiVersion();
    expect(result).toBeNull();
  });

  it("should return null and log an error when the fetch throws an error", async () => {
    vi.mocked(fetch).mockRejectedValue(new Error("Network error"));

    const result = await ApiVersion();
    expect(result).toBeNull();
    expect(console.error).toHaveBeenCalledWith(
      "Error fetching API version:",
      expect.any(Error),
    );
  });

  it("should use default API URL if NEXT_PUBLIC_API_URL is not set", async () => {
    const originalEnv = process.env.NEXT_PUBLIC_API_URL;
    delete process.env.NEXT_PUBLIC_API_URL;

    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ build: { version: "1.0.0" } }),
    } as Response);

    await ApiVersion();

    expect(fetch).toHaveBeenCalledWith(
      "http://localhost:8080/actuator/info",
      expect.any(Object),
    );

    process.env.NEXT_PUBLIC_API_URL = originalEnv;
  });

  it("should remove trailing slash from API URL", async () => {
    const originalEnv = process.env.NEXT_PUBLIC_API_URL;
    process.env.NEXT_PUBLIC_API_URL = "http://api.test/";

    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ build: { version: "1.0.0" } }),
    } as Response);

    await ApiVersion();

    expect(fetch).toHaveBeenCalledWith(
      "http://api.test/actuator/info",
      expect.any(Object),
    );

    process.env.NEXT_PUBLIC_API_URL = originalEnv;
  });
});
