import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { InvitationCardProps } from "@/components/invitations/invitation-card";
import { listPendingInvitations } from "@/generated";
import { auth0 } from "@/lib/auth0";
import { mockPartial } from "@/lib/test-utils";
import { PendingInvitations } from "./pending-invitations";

vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn(),
  },
}));

vi.mock("@/generated/sdk.gen", () => ({
  listPendingInvitations: vi.fn(),
}));

// Mock InvitationCard to avoid testing it recursively here
vi.mock("./invitation-card", () => ({
  InvitationCard: ({
    id,
    householdName,
    householdRole,
  }: InvitationCardProps) => (
    <div data-testid="invitation-card">
      {id} - {householdName} - {householdRole}
    </div>
  ),
}));

describe("PendingInvitations", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("console", { ...console, error: vi.fn() });
  });

  afterEach(() => {
    cleanup();
  });

  it("should return null when no session exists", async () => {
    vi.mocked(auth0.getSession).mockResolvedValue(null);
    const result = await PendingInvitations();
    expect(result).toBeNull();
  });

  it("should return null when no invitations exist", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({ user: { name: "Test" } });
    mockPartial(listPendingInvitations).mockResolvedValue({
      data: { invitations: [] },
    });

    const result = await PendingInvitations();
    expect(result).toBeNull();
  });

  it("should render invitations when they exist", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({ user: { name: "Test" } });
    mockPartial(listPendingInvitations).mockResolvedValue({
      data: {
        invitations: [
          { id: "inv-1", householdName: "House 1", role: "MEMBER" },
          { id: "inv-2", householdName: "House 2", role: "OWNER" },
        ],
      },
    });

    render(await PendingInvitations());

    expect(screen.getByText("Pending Invitations")).toBeInTheDocument();
    const cards = screen.getAllByTestId("invitation-card");
    expect(cards).toHaveLength(2);
    expect(screen.getByText(/inv-1 - House 1 - MEMBER/)).toBeInTheDocument();
    expect(screen.getByText(/inv-2 - House 2 - OWNER/)).toBeInTheDocument();
  });

  it("should log error and return null on fetch failure", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({ user: { name: "Test" } });
    vi.mocked(listPendingInvitations).mockRejectedValue(new Error("API Error"));

    const result = await PendingInvitations();
    expect(result).toBeNull();
    expect(console.error).toHaveBeenCalledWith(
      "Failed to fetch pending invitations:",
      expect.any(Error),
    );
  });
});
