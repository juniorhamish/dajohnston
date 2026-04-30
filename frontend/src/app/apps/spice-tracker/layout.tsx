import type { ReactNode } from "react";
import { AppLayout } from "@/components/layouts/app-layout";

export default function SpiceTrackerLayout({
  children,
}: Readonly<{
  children: ReactNode;
}>) {
  return (
    <div className="spice-tracker min-h-screen">
      <AppLayout>{children}</AppLayout>
    </div>
  );
}
