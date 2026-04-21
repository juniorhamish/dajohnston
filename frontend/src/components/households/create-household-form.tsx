"use client";

import { useState, useTransition } from "react";
import { Button } from "@/components/ui/button";
import { createHouseholdAction } from "./household-actions";

export function CreateHouseholdForm() {
  const [isOpen, setIsOpen] = useState(false);
  const [isPending, startTransition] = useTransition();

  function handleSubmit(formData: FormData) {
    const name = formData.get("name") as string;
    startTransition(async () => {
      try {
        await createHouseholdAction(name);
        setIsOpen(false);
      } catch (e) {
        console.error("Failed to create household:", e);
      }
    });
  }

  if (!isOpen) {
    return (
      <Button
        variant="outline"
        onClick={() => setIsOpen(true)}
        className="w-full text-xs font-bold uppercase tracking-widest mt-4"
      >
        + Create New Household
      </Button>
    );
  }

  return (
    <div className="mt-4 p-4 bg-accent/30 rounded-lg border">
      <h4 className="text-sm font-bold mb-3">Create New Household</h4>
      <form action={handleSubmit} className="flex flex-col gap-3">
        <div>
          <label
            htmlFor="name"
            className="block text-[10px] font-bold text-muted-foreground uppercase mb-1"
          >
            Household Name
          </label>
          <input
            type="text"
            id="name"
            name="name"
            required
            className="w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-ring bg-background"
            placeholder="My Sweet Home"
          />
        </div>
        <div className="flex gap-2 mt-1">
          <Button
            type="submit"
            disabled={isPending}
            className="flex-1"
            size="sm"
          >
            {isPending ? "Creating..." : "Create Household"}
          </Button>
          <Button
            type="button"
            variant="secondary"
            onClick={() => setIsOpen(false)}
            size="sm"
          >
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
}
