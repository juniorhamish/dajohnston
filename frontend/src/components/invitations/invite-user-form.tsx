"use client";

import { useState, useTransition } from "react";
import { Button } from "@/components/ui/button";
import { inviteUserAction } from "./invitation-actions";

export interface InviteUserFormProps {
  householdId: string;
  householdName: string;
}

export function InviteUserForm({
  householdId,
  householdName,
}: Readonly<InviteUserFormProps>) {
  const [isOpen, setIsOpen] = useState(false);
  const [isPending, startTransition] = useTransition();

  function handleSubmit(formData: FormData) {
    startTransition(async () => {
      try {
        await inviteUserAction(householdId, formData);
        setIsOpen(false);
      } catch (e) {
        console.error("Failed to invite user:", e);
      }
    });
  }

  if (!isOpen) {
    return (
      <Button
        variant="ghost"
        size="sm"
        onClick={() => setIsOpen(true)}
        className="text-[10px] font-bold uppercase tracking-wider h-auto p-0 hover:bg-transparent text-primary hover:text-primary/80"
      >
        + Invite User
      </Button>
    );
  }

  return (
    <div className="mt-4 p-4 bg-accent/30 rounded-lg border">
      <h4 className="text-sm font-bold mb-3">Invite to {householdName}</h4>
      <form action={handleSubmit} className="flex flex-col gap-3">
        <div>
          <label
            htmlFor="email"
            className="block text-[10px] font-bold text-muted-foreground uppercase mb-1"
          >
            Email Address
          </label>
          <input
            type="email"
            id="email"
            name="email"
            required
            className="w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-ring bg-background"
            placeholder="user@example.com"
          />
        </div>
        <div>
          <label
            htmlFor="role"
            className="block text-[10px] font-bold text-muted-foreground uppercase mb-1"
          >
            Role
          </label>
          <select
            id="role"
            name="role"
            required
            className="w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-ring bg-background"
            defaultValue="MEMBER"
          >
            <option value="MEMBER">Member</option>
            <option value="OWNER">Owner</option>
          </select>
        </div>
        <div className="flex gap-2 mt-1">
          <Button
            type="submit"
            disabled={isPending}
            className="flex-1"
            size="sm"
          >
            {isPending ? "Sending..." : "Send Invitation"}
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
