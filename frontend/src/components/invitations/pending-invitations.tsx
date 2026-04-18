import { listPendingInvitations } from "@/generated";
import { auth0 } from "@/lib/auth0";
import { InvitationCard } from "./invitation-card";

export async function PendingInvitations() {
  const session = await auth0.getSession();
  if (!session?.user) {
    return null;
  }

  const { data } = await listPendingInvitations().catch((e) => {
    console.error("Failed to fetch pending invitations:", e);
    return { data: undefined };
  });

  const invitations = data?.invitations ?? [];

  if (invitations.length === 0) {
    return null;
  }

  return (
    <div className="w-full">
      <h2 className="text-xl font-bold text-gray-900 mb-4">
        Pending Invitations
      </h2>
      <div className="grid gap-3">
        {invitations.map((invitation) => (
          <InvitationCard
            key={invitation.id}
            id={invitation.id}
            householdName={invitation.householdName}
            householdRole={invitation.role}
          />
        ))}
      </div>
    </div>
  );
}
