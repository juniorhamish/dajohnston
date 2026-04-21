import Image from "next/image";
import { CreateHouseholdForm } from "@/components/households/create-household-form";
import { DeleteHouseholdButton } from "@/components/households/delete-household-button";
import { InviteUserForm } from "@/components/invitations/invite-user-form";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getCurrentUser } from "@/generated";
import { auth0 } from "@/lib/auth0";

export async function UserProfileCard() {
  const session = await auth0.getSession();
  const user = session?.user;

  if (!user) {
    return null;
  }
  const { data: userProfile } = await getCurrentUser().catch((e) => {
    console.error("Failed to fetch user mapping:", e);
    return { data: undefined };
  });

  return (
    <Card className="w-full">
      <CardHeader>
        <div className="flex items-center gap-6">
          {(userProfile?.picture || user.picture) && (
            <Image
              width={80}
              height={80}
              src={(userProfile?.picture || user.picture) as string}
              alt={userProfile?.nickname || user.name || "User Profile Picture"}
              className="w-20 h-20 rounded-full shadow-sm border-2 border-primary/10"
            />
          )}
          <div className="flex-1">
            <CardTitle className="text-2xl leading-tight">
              {userProfile?.nickname || user.name}
            </CardTitle>
            <p className="text-muted-foreground font-medium">
              {userProfile?.email || user.email}
            </p>
            {userProfile?.givenName && (
              <p className="text-sm text-muted-foreground/80">
                {userProfile.givenName} {userProfile.familyName}
              </p>
            )}
            {userProfile?.nickname && (
              <p className="text-sm text-muted-foreground/60 italic">
                @{userProfile.nickname}
              </p>
            )}
            {userProfile?.id && (
              <p className="text-[10px] text-muted-foreground/40 mt-1 font-mono uppercase tracking-wider">
                ID: {userProfile?.id}
              </p>
            )}
          </div>
        </div>
      </CardHeader>

      <CardContent>
        <div className="pt-6 border-t">
          <h3 className="text-xs font-bold uppercase tracking-widest mb-4 text-muted-foreground">
            Household & Tenant Mapping
          </h3>

          {userProfile?.households && userProfile?.households.length > 0 ? (
            <div className="grid gap-3">
              {userProfile?.households.map((h) => (
                <div
                  key={h.id}
                  className="flex flex-col bg-accent/50 p-4 rounded-lg border border-border/50"
                >
                  <div className="flex items-center justify-between mb-2">
                    <span className="font-semibold">{h.name}</span>
                    <span className="text-[10px] font-bold uppercase px-2 py-1 bg-primary text-primary-foreground rounded-md">
                      {h.role}
                    </span>
                  </div>
                  {h.role === "OWNER" && (
                    <div className="mt-2 pt-2 border-t border-border/50 flex justify-between items-center">
                      <InviteUserForm
                        householdId={h.id}
                        householdName={h.name}
                      />
                      <DeleteHouseholdButton
                        householdId={h.id}
                        householdName={h.name}
                      />
                    </div>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <div className="bg-destructive/10 px-4 py-3 rounded-lg border border-destructive/20 text-destructive text-sm font-medium">
              No household assigned. Contact your administrator or create one.
            </div>
          )}
          <CreateHouseholdForm />
        </div>
      </CardContent>
    </Card>
  );
}
