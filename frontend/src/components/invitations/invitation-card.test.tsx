import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { InvitationCard } from "./invitation-card";

vi.mock("./invitation-actions", () => ({
  acceptInvitationAction: vi.fn(),
  declineInvitationAction: vi.fn(),
}));

describe("InvitationCard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    cleanup();
  });

  it("should render invitation details", () => {
    render(
      <InvitationCard
        id="inv-1"
        householdName="My House"
        householdRole="MEMBER"
      />,
    );

    expect(screen.getByText("My House")).toBeInTheDocument();
    expect(screen.getByText("MEMBER")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: /Accept/i })).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /Decline/i }),
    ).toBeInTheDocument();
  });

  it("should have correct form actions", () => {
    render(
      <InvitationCard
        id="inv-1"
        householdName="My House"
        householdRole="MEMBER"
      />,
    );

    expect(
      screen.getByRole("form", { name: /Accept invitation/i }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("form", { name: /Decline invitation/i }),
    ).toBeInTheDocument();
  });
});
