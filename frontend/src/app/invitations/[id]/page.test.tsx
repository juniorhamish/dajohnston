import { cleanup, render, screen } from "@testing-library/react";
import { redirect } from "next/navigation";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import type { InvitationCardProps } from "@/components/invitations/invitation-card";
import { listPendingInvitations } from "@/generated";
import { auth0 } from "@/lib/auth0";
import { mockPartial } from "@/lib/test-utils";
import InvitationPage from "./page";

vi.mock("@/lib/auth0", () => ({
  auth0: {
    getSession: vi.fn(),
  },
}));

vi.mock("@/generated/sdk.gen", () => ({
  listPendingInvitations: vi.fn(),
}));

vi.mock("next/navigation", () => ({
  redirect: vi.fn(),
}));

vi.mock("@/components/invitations/invitation-card", () => ({
  InvitationCard: ({ id, householdName }: InvitationCardProps) => (
    <div data-testid="inv-card">
      {id} - {householdName}
    </div>
  ),
}));

describe("InvitationPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    vi.stubGlobal("console", { ...console, error: vi.fn() });
  });

  afterEach(() => {
    cleanup();
  });

  it("should redirect to login if no session exists", async () => {
    vi.mocked(auth0.getSession).mockResolvedValue(null);

    await InvitationPage({ params: Promise.resolve({ id: "inv-1" }) });

    expect(redirect).toHaveBeenCalledWith("/api/auth/login");
  });

  it("should show not found message if invitation does not exist", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({ user: { name: "Test" } });
    mockPartial(listPendingInvitations).mockResolvedValue({
      data: { invitations: [] },
    });

    render(await InvitationPage({ params: Promise.resolve({ id: "inv-1" }) }));

    expect(screen.getByText("Invitation Not Found")).toBeInTheDocument();
  });

  it("should render invitation details if found", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({ user: { name: "Test" } });
    mockPartial(listPendingInvitations).mockResolvedValue({
      data: {
        invitations: [
          {
            id: "inv-1",
            householdName: "Cool House",
            role: "MEMBER",
            status: "PENDING",
            email: "test@example.com",
            householdId: "h1",
          },
        ],
      },
    });

    render(await InvitationPage({ params: Promise.resolve({ id: "inv-1" }) }));

    expect(screen.getByText("Household Invitation")).toBeInTheDocument();
    expect(screen.getByTestId("inv-card")).toHaveTextContent(
      "inv-1 - Cool House",
    );
  });

  it("should show not found message if listPendingInvitations fails", async () => {
    mockPartial(auth0.getSession).mockResolvedValue({ user: { name: "Test" } });
    mockPartial(listPendingInvitations).mockRejectedValue(new Error("API Error"));

    render(await InvitationPage({ params: Promise.resolve({ id: "inv-1" }) }));

    expect(screen.getByText("Invitation Not Found")).toBeInTheDocument();
    expect(console.error).toHaveBeenCalledWith(
      "Failed to fetch pending invitations:",
      expect.any(Error),
    );
  });
});
