import { redirect } from "next/navigation";
import { InvitationCard } from "@/components/invitations/invitation-card";
import { listPendingInvitations } from "@/generated";
import { auth0 } from "@/lib/auth0";

export default async function InvitationPage({
  params,
}: Readonly<{
  params: Promise<{ id: string }>;
}>) {
  const { id } = await params;
  const session = await auth0.getSession();

  if (!session?.user) {
    // Redirect to login if not authenticated
    // Note: Auth0 middleware or layout usually handles this, but being explicit is fine.
    return redirect("/api/auth/login");
  }

  const { data } = await listPendingInvitations().catch((e) => {
    console.error("Failed to fetch pending invitations:", e);
    return { data: undefined };
  });

  const invitation = data?.invitations.find((i) => i.id === id);

  if (!invitation) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen p-8">
        <div className="bg-red-50 p-8 rounded-xl border border-red-100 max-w-md w-full text-center">
          <h1 className="text-2xl font-bold text-red-900 mb-4">
            Invitation Not Found
          </h1>
          <p className="text-red-700 mb-6">
            The invitation you are looking for may have expired, already been
            accepted, or you do not have permission to view it.
          </p>
          <a
            href="/"
            className="inline-block px-6 py-3 bg-red-600 text-white font-medium rounded-lg hover:bg-red-700 transition-colors"
          >
            Go to Dashboard
          </a>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-8 bg-gray-50">
      <div className="bg-white p-8 rounded-2xl shadow-xl border border-gray-100 max-w-lg w-full">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Household Invitation
          </h1>
          <p className="text-gray-500">
            You have been invited to join a household.
          </p>
        </div>

        <div className="mb-8">
          <InvitationCard
            id={invitation.id}
            householdName={invitation.householdName}
            householdRole={invitation.role}
          />
        </div>

        <div className="text-center">
          <p className="text-sm text-gray-400">
            Accepting this invitation will give you access to the
            household&apos;s shared resources as a{" "}
            {invitation.role.toLowerCase()}.
          </p>
        </div>
      </div>
    </div>
  );
}
