"use client";

import { useTransition } from "react";
import { Button } from "@/components/ui/button";
import { deleteHouseholdAction } from "./household-actions";

export interface DeleteHouseholdButtonProps {
  householdId: string;
  householdName: string;
}

export function DeleteHouseholdButton({
  householdId,
  householdName,
}: Readonly<DeleteHouseholdButtonProps>) {
  const [isPending, startTransition] = useTransition();

  const handleDelete = () => {
    if (
      window.confirm(
        `Are you sure you want to delete the household "${householdName}"? This action can be undone by an administrator.`,
      )
    ) {
      startTransition(async () => {
        try {
          await deleteHouseholdAction(householdId);
        } catch (error) {
          console.error("Failed to delete household:", error);
          alert("Failed to delete household. Please try again.");
        }
      });
    }
  };

  return (
    <Button
      variant="ghost"
      size="sm"
      onClick={handleDelete}
      disabled={isPending}
      className="text-[10px] font-bold uppercase tracking-wider h-auto p-0 hover:bg-transparent text-destructive hover:text-destructive/80"
    >
      {isPending ? "Deleting..." : "Delete Household"}
    </Button>
  );
}
