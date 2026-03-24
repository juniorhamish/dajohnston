import { NextResponse } from "next/server";
import { describe, expect, it, vi } from "vitest";
import { auth0 } from "./lib/auth0";
import { proxy } from "./proxy";

vi.mock("./lib/auth0", () => ({
  auth0: {
    middleware: vi.fn(),
  },
}));

describe("proxy", () => {
  it("should call auth0.middleware with the request", async () => {
    const request = new Request("http://localhost:3000/api/test");
    const response = new NextResponse("ok");
    vi.mocked(auth0.middleware).mockResolvedValue(response);

    const result = await proxy(request);

    expect(auth0.middleware).toHaveBeenCalledWith(request);
    expect(result).toBe(response);
  });
});
