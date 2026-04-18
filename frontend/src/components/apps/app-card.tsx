import Link from "next/link";
import {
  Card,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { App } from "@/generated";

interface AppCardProps {
  app: App;
}

export function AppCard({ app }: Readonly<AppCardProps>) {
  return (
    <Link href={app.url} className="group">
      <Card className="h-full hover:shadow-md transition-all duration-200 hover:border-primary/20">
        <CardHeader>
          <div className="flex items-center gap-3">
            {app.icon && (
              <span
                className="text-2xl"
                role="img"
                aria-label={`${app.name} icon`}
              >
                {app.icon}
              </span>
            )}
            <CardTitle className="group-hover:text-primary transition-colors">
              {app.name}
            </CardTitle>
          </div>
          {app.description && (
            <CardDescription className="mt-2">
              {app.description}
            </CardDescription>
          )}
        </CardHeader>
      </Card>
    </Link>
  );
}
