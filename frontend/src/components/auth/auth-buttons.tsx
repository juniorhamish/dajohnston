import { Button } from "@/components/ui/button";
import { auth0 } from "@/lib/auth0";

export async function AuthButtons() {
  const session = await auth0.getSession();
  const user = session?.user;

  if (user) {
    return (
      <div className="flex items-center gap-4">
        <span className="text-sm text-muted-foreground">
          Welcome, {user.name}
        </span>
        <Button asChild variant="destructive" size="sm">
          <a href="/auth/logout">Logout</a>
        </Button>
      </div>
    );
  }

  return (
    <Button asChild size="sm">
      <a href="/auth/login">Login</a>
    </Button>
  );
}
