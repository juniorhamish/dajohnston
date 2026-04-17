import Link from "next/link";
import type { App } from "@/generated";

interface AppCardProps {
  app: App;
}

export function AppCard({ app }: Readonly<AppCardProps>) {
  return (
    <Link
      href={app.url}
      className="group flex flex-col gap-3 p-6 bg-white rounded-xl shadow-lg border border-gray-100 hover:shadow-xl hover:border-primary/20 transition-all duration-200"
    >
      {app.icon && (
        <span className="text-3xl" role="img" aria-label={`${app.name} icon`}>
          {app.icon}
        </span>
      )}
      <h3 className="text-lg font-bold text-gray-900 group-hover:text-primary transition-colors">
        {app.name}
      </h3>
      {app.description && (
        <p className="text-sm text-gray-500 leading-relaxed">
          {app.description}
        </p>
      )}
    </Link>
  );
}
