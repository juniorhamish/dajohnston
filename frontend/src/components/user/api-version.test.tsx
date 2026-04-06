import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { getApplicationInfo } from "@/generated";
import { mockPartial } from "@/lib/test-utils";
import { ApiVersion } from "./api-version";

vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn().mockResolvedValue(null),
  },
}));
vi.mock("@/generated/sdk.gen", () => ({
  getApplicationInfo: vi.fn(),
}));

describe("ApiVersion", () => {
  beforeEach(() => {
    vi.stubGlobal("console", { ...console, error: vi.fn() });
    vi.clearAllMocks();
  });
  it("should render the API version when the fetch is successful", async () => {
    mockPartial(getApplicationInfo).mockResolvedValue({
      data: { build: { version: "1.2.3" } },
    });

    render(await ApiVersion());

    expect.assertions(1);
    expect(screen.getByText("API Version: 1.2.3")).toBeInTheDocument();
  });
  it("should return null when the fetch fails", async () => {
    vi.mocked(getApplicationInfo).mockRejectedValue(new Error("Network error"));

    expect.assertions(2);
    expect(await ApiVersion()).toBeNull();
    expect(console.error).toHaveBeenCalledWith(
      "Error fetching API version:",
      new Error("Network error"),
    );
  });
  it("should return null when the version is missing from the data", async () => {
    mockPartial(getApplicationInfo).mockResolvedValue({
      data: { build: {} },
    });

    expect(await ApiVersion()).toBeNull();
  });
  it("should return null when the build info is missing from the data", async () => {
    mockPartial(getApplicationInfo).mockResolvedValue({
      data: {},
    });

    expect(await ApiVersion()).toBeNull();
  });
});
