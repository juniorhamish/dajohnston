"use client";

import type { ChangeEvent } from "react";
import { useTransition } from "react";
import { setActiveHousehold } from "@/app/actions/household";
import type { Household } from "@/generated";

interface HouseholdSwitcherProps {
  households: Household[];
  activeHouseholdId?: string;
}

export function HouseholdSwitcher({
  households,
  activeHouseholdId,
}: Readonly<HouseholdSwitcherProps>) {
  const [isPending, startTransition] = useTransition();

  const handleSelect = (e: ChangeEvent<HTMLSelectElement>) => {
    const id = e.target.value;
    startTransition(() => {
      setActiveHousehold(id);
    });
  };

  if (households.length === 0) return null;

  return (
    <div className="flex items-center gap-2">
      <label
        htmlFor="household-select"
        className="text-sm font-medium text-muted-foreground hidden sm:inline"
      >
        Household:
      </label>
      <select
        id="household-select"
        className="bg-background border border-input rounded px-2 py-1 text-sm focus:ring-2 focus:ring-primary outline-none disabled:opacity-50"
        value={activeHouseholdId ?? ""}
        onChange={handleSelect}
        disabled={isPending}
      >
        <option value="" disabled>
          Select a household
        </option>
        {households.map((h) => (
          <option key={h.id} value={h.id}>
            {h.name}
          </option>
        ))}
      </select>
    </div>
  );
}
