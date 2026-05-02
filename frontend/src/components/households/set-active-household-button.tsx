"use client";

import { Check } from "lucide-react";
import { useTransition } from "react";
import { setActiveHousehold } from "@/app/actions/household";
import { Button } from "@/components/ui/button";

interface SetActiveHouseholdButtonProps {
  householdId: string;
  isActive: boolean;
}

export function SetActiveHouseholdButton({
  householdId,
  isActive,
}: Readonly<SetActiveHouseholdButtonProps>) {
  const [isPending, startTransition] = useTransition();

  if (isActive) {
    return (
      <div className="flex items-center gap-1 text-xs font-medium text-green-600 bg-green-50 px-2 py-1 rounded-md border border-green-100">
        <Check className="h-3 w-3" /> Active
      </div>
    );
  }

  return (
    <Button
      variant="outline"
      size="sm"
      className="h-8 text-[10px] uppercase font-bold"
      onClick={() => startTransition(() => setActiveHousehold(householdId))}
      disabled={isPending}
    >
      Set Active
    </Button>
  );
}
