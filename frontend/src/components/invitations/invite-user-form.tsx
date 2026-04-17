"use client";

import { useState } from "react";
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
  const [isPending, setIsPending] = useState(false);

  async function handleSubmit(formData: FormData) {
    setIsPending(true);
    try {
      await inviteUserAction(householdId, formData);
      setIsOpen(false);
    } catch (e) {
      console.error("Failed to invite user:", e);
    } finally {
      setIsPending(false);
    }
  }

  if (!isOpen) {
    return (
      <button
        type="button"
        onClick={() => setIsOpen(true)}
        className="text-xs font-bold text-blue-600 hover:text-blue-800 transition-colors cursor-pointer uppercase tracking-wider"
      >
        + Invite User
      </button>
    );
  }

  return (
    <div className="mt-4 p-4 bg-gray-50 rounded-lg border border-gray-200">
      <h4 className="text-sm font-bold text-gray-900 mb-3">
        Invite to {householdName}
      </h4>
      <form action={handleSubmit} className="flex flex-col gap-3">
        <div>
          <label
            htmlFor="email"
            className="block text-xs font-medium text-gray-500 uppercase mb-1"
          >
            Email Address
          </label>
          <input
            type="email"
            id="email"
            name="email"
            required
            className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            placeholder="user@example.com"
          />
        </div>
        <div>
          <label
            htmlFor="role"
            className="block text-xs font-medium text-gray-500 uppercase mb-1"
          >
            Role
          </label>
          <select
            id="role"
            name="role"
            required
            className="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
            defaultValue="MEMBER"
          >
            <option value="MEMBER">Member</option>
            <option value="OWNER">Owner</option>
          </select>
        </div>
        <div className="flex gap-2 mt-1">
          <button
            type="submit"
            disabled={isPending}
            className="flex-1 px-3 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50 transition-colors cursor-pointer"
          >
            {isPending ? "Sending..." : "Send Invitation"}
          </button>
          <button
            type="button"
            onClick={() => setIsOpen(false)}
            className="px-3 py-2 text-sm font-medium text-gray-700 bg-gray-200 rounded-md hover:bg-gray-300 transition-colors cursor-pointer"
          >
            Cancel
          </button>
        </div>
      </form>
    </div>
  );
}
