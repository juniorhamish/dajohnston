import { AppGrid } from "@/components/apps/app-grid";
import { PendingInvitations } from "@/components/invitations/pending-invitations";
import { AppLayout } from "@/components/layouts/app-layout";
import { UserProfileCard } from "@/components/user/user-profile";

export const dynamic = "force-dynamic";

export default async function Home() {
  return (
    <AppLayout>
      <div className="flex flex-col gap-8 items-center sm:items-start w-full max-w-4xl mx-auto">
        <h1 className="text-4xl font-bold">Portal Dashboard</h1>
        <p className="text-lg text-center sm:text-left text-muted-foreground">
          Welcome to your centralized hub for sub-applications.
        </p>
        <UserProfileCard />
        <PendingInvitations />
        <AppGrid />
      </div>
    </AppLayout>
  );
}
