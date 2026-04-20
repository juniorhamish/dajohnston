import type { ReactNode } from "react";
import { Navbar } from "@/components/navbar";
import { ApiVersion } from "@/components/user/api-version";

interface AppLayoutProps {
  children: ReactNode;
}

export function AppLayout({ children }: Readonly<AppLayoutProps>) {
  return (
    <div className="relative flex min-h-screen flex-col">
      <Navbar />
      <main className="flex-1">
        <div className="container mx-auto px-4 py-8 md:px-8">{children}</div>
      </main>
      <footer className="border-t py-6 md:py-0">
        <div className="container mx-auto flex flex-col items-center justify-between gap-4 px-4 md:h-24 md:flex-row md:px-8">
          <p className="text-balance text-center text-sm leading-loose text-muted-foreground md:text-left">
            &copy; {new Date().getFullYear()} uk.co.dajohnston. All rights
            reserved.
          </p>
          <div className="flex items-center gap-4">
            <ApiVersion />
          </div>
        </div>
      </footer>
    </div>
  );
}
