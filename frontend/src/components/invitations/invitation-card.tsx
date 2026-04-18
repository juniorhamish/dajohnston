"use client";

import { Button } from "@/components/ui/button";
import {
  acceptInvitationAction,
  declineInvitationAction,
} from "./invitation-actions";

export interface InvitationCardProps {
  id: string;
  householdName: string;
  householdRole: string;
}

export function InvitationCard({
  id,
  householdName,
  householdRole,
}: Readonly<InvitationCardProps>) {
  return (
    <div className="flex items-center justify-between bg-accent/30 px-4 py-3 rounded-lg border">
      <div>
        <span className="font-semibold">{householdName}</span>
        <span className="ml-2 text-[10px] font-bold uppercase px-2 py-1 bg-primary text-primary-foreground rounded-md">
          {householdRole}
        </span>
      </div>
      <div className="flex gap-2">
        <form
          action={acceptInvitationAction.bind(null, id)}
          aria-label="Accept invitation"
        >
          <Button
            type="submit"
            size="sm"
            className="bg-green-600 hover:bg-green-700 text-white"
          >
            Accept
          </Button>
        </form>
        <form
          action={declineInvitationAction.bind(null, id)}
          aria-label="Decline invitation"
        >
          <Button type="submit" variant="secondary" size="sm">
            Decline
          </Button>
        </form>
      </div>
    </div>
  );
}
