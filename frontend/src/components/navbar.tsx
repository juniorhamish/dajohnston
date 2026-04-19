import Link from "next/link";
import { AuthButtons } from "@/components/auth/auth-buttons";
import { NotificationManager } from "@/components/notifications/notification-manager";
import { ThemeToggle } from "@/components/theme-toggle";

export function Navbar() {
  return (
    <nav className="sticky top-0 z-40 w-full border-b bg-background/95 backdrop-blur supports-backdrop-filter:bg-background/60">
      <div className="container flex h-16 items-center justify-between gap-2 sm:gap-4">
        <div className="flex min-w-0 items-center gap-4 md:gap-10">
          <Link href="/" className="flex items-center space-x-2">
            <span className="inline-block truncate font-bold">
              Multi-App Portal
            </span>
          </Link>
          <div className="hidden md:flex gap-6">
            <Link
              href="/"
              className="flex items-center text-sm font-medium text-muted-foreground hover:text-foreground"
            >
              Dashboard
            </Link>
            {/* Future apps links can be added here or via a sidebar */}
          </div>
        </div>
        <div className="flex shrink-0 items-center gap-1 sm:gap-2 md:gap-4">
          <NotificationManager />
          <ThemeToggle />
          <AuthButtons />
        </div>
      </div>
    </nav>
  );
}
