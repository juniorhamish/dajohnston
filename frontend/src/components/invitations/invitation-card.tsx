"use client";

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
    <div className="flex items-center justify-between bg-green-50/50 px-4 py-3 rounded-lg border border-green-100/50">
      <div>
        <span className="font-semibold text-green-900">{householdName}</span>
        <span className="ml-2 text-xs font-bold uppercase px-2 py-1 bg-green-100 text-green-700 rounded-md">
          {householdRole}
        </span>
      </div>
      <div className="flex gap-2">
        <form
          action={acceptInvitationAction.bind(null, id)}
          aria-label="Accept invitation"
        >
          <button
            type="submit"
            className="px-3 py-1.5 text-sm font-medium text-white bg-green-600 rounded-md hover:bg-green-700 transition-colors cursor-pointer"
          >
            Accept
          </button>
        </form>
        <form
          action={declineInvitationAction.bind(null, id)}
          aria-label="Decline invitation"
        >
          <button
            type="submit"
            className="px-3 py-1.5 text-sm font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 transition-colors cursor-pointer"
          >
            Decline
          </button>
        </form>
      </div>
    </div>
  );
}
