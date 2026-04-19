import { Auth0Client } from "@auth0/nextjs-auth0/server";
import { describe, expect, it, vi } from "vitest";

vi.mock("server-only", () => ({}));
vi.mock("@auth0/nextjs-auth0/server", () => ({
  Auth0Client: vi.fn(),
}));

describe("auth0", () => {
  it("should initialize Auth0Client", async () => {
    await import("./auth0");
    expect(Auth0Client).toHaveBeenCalled();
  });
});
