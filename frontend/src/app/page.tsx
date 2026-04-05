export const dynamic = "force-dynamic";

import {ApiVersion} from "@/components/user/api-version";
import {AuthButtons} from "@/components/auth/auth-buttons";
import {UserProfile} from "@/components/user/user-profile";

export default async function Home() {
  const params = await Promise.resolve({}); // Mocking async params if they were used
  return (
      <div
          className="grid grid-rows-[20px_1fr_20px] items-center justify-items-center min-h-screen p-8 pb-20 gap-16 sm:p-20 font-(family-name:--font-geist-sans)">
        <header className="row-start-1 w-full flex justify-end p-4">
          <AuthButtons/>
        </header>
        <main
            className="flex flex-col gap-8 row-start-2 items-center sm:items-start w-full max-w-2xl">
          <h1 className="text-4xl font-bold">Multi-App Portal</h1>
          <p className="text-lg text-center sm:text-left">
            Welcome to your centralized hub for sub-applications.
          </p>
          <UserProfile/>
        </main>
        <footer className="row-start-3 flex gap-6 flex-wrap items-center justify-center">
          <p>&copy; {new Date().getFullYear()} uk.co.dajohnston</p>
          <ApiVersion/>
        </footer>
      </div>
  );
}
