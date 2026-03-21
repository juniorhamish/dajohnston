import { auth0 } from "@/lib/auth0";

export async function AuthButtons() {
  const session = await auth0.getSession();
  const user = session?.user;

  if (user) {
    return (
      <div className="flex items-center gap-4">
        <span>Welcome, {user.name}</span>
        <a
          href="/auth/logout"
          className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition-colors"
        >
          Logout
        </a>
      </div>
    );
  }

  return (
    <a
      href="/auth/login"
      className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
    >
      Login
    </a>
  );
}
