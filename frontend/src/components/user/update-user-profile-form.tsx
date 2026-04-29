"use client";

import { useState, useTransition } from "react";
import { Button } from "@/components/ui/button";
import type { UpdateUserProfileRequest, UserProfile } from "@/generated";
import { updateUserProfileAction } from "./user-actions";

interface UpdateUserProfileFormProps {
  userProfile: UserProfile;
  onCancel: () => void;
}

export function UpdateUserProfileForm({
  userProfile,
  onCancel,
}: Readonly<UpdateUserProfileFormProps>) {
  const [isPending, startTransition] = useTransition();
  const [useGravatar, setUseGravatar] = useState(userProfile.useGravatar);
  const [manualPictureUrl, setManualPictureUrl] = useState(
    userProfile.manualPictureUrl ||
      (userProfile.useGravatar ? "" : (userProfile.picture ?? "")),
  );

  function handleSubmit(formData: FormData) {
    const data: UpdateUserProfileRequest = {
      givenName: (formData.get("givenName") as string) || undefined,
      familyName: (formData.get("familyName") as string) || undefined,
      nickname: (formData.get("nickname") as string) || undefined,
      useGravatar: formData.get("useGravatar") === "on",
    };

    if (!data.useGravatar) {
      data.picture = manualPictureUrl || undefined;
    }

    startTransition(async () => {
      try {
        await updateUserProfileAction(data);
        onCancel();
      } catch (e) {
        console.error("Failed to update profile:", e);
      }
    });
  }

  return (
    <div className="mt-4 p-4 bg-accent/30 rounded-lg border">
      <h4 className="text-sm font-bold mb-3">Edit Profile</h4>
      <form
        aria-label="Edit Profile"
        action={handleSubmit}
        className="flex flex-col gap-4"
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label
              htmlFor="givenName"
              className="block text-[10px] font-bold text-muted-foreground uppercase mb-1"
            >
              Given Name
            </label>
            <input
              type="text"
              id="givenName"
              name="givenName"
              defaultValue={userProfile.givenName ?? ""}
              className="w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-ring bg-background"
              placeholder="John"
            />
          </div>
          <div>
            <label
              htmlFor="familyName"
              className="block text-[10px] font-bold text-muted-foreground uppercase mb-1"
            >
              Family Name
            </label>
            <input
              type="text"
              id="familyName"
              name="familyName"
              defaultValue={userProfile.familyName ?? ""}
              className="w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-ring bg-background"
              placeholder="Doe"
            />
          </div>
        </div>

        <div>
          <label
            htmlFor="nickname"
            className="block text-[10px] font-bold text-muted-foreground uppercase mb-1"
          >
            Nickname
          </label>
          <input
            type="text"
            id="nickname"
            name="nickname"
            defaultValue={userProfile.nickname ?? ""}
            className="w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-ring bg-background"
            placeholder="johndoe"
          />
        </div>

        <div className="flex items-center gap-2">
          <input
            type="checkbox"
            id="useGravatar"
            name="useGravatar"
            checked={useGravatar}
            onChange={(e) => setUseGravatar(e.target.checked)}
            className="w-4 h-4 rounded border-gray-300 text-primary focus:ring-primary"
          />
          <label
            htmlFor="useGravatar"
            className="text-sm font-medium leading-none peer-disabled:cursor-not-allowed peer-disabled:opacity-70"
          >
            Use Gravatar (generated from email)
          </label>
        </div>

        <div>
          <label
            htmlFor="picture"
            className="block text-[10px] font-bold text-muted-foreground uppercase mb-1"
          >
            Profile Picture URL
          </label>
          <input
            type="text"
            id="picture"
            name="picture"
            value={useGravatar ? (userProfile.picture ?? "") : manualPictureUrl}
            onChange={(e) => setManualPictureUrl(e.target.value)}
            disabled={useGravatar}
            className="w-full px-3 py-2 text-sm border rounded-md focus:outline-none focus:ring-2 focus:ring-ring bg-background disabled:opacity-50 disabled:cursor-not-allowed"
            placeholder="https://example.com/avatar.png"
          />
          {useGravatar && (
            <p className="text-[10px] text-muted-foreground mt-1">
              Picture is automatically managed via Gravatar.
            </p>
          )}
        </div>

        <div className="flex gap-2 mt-2">
          <Button
            type="submit"
            disabled={isPending}
            className="flex-1"
            size="sm"
          >
            {isPending ? "Saving..." : "Save Changes"}
          </Button>
          <Button
            type="button"
            variant="secondary"
            onClick={onCancel}
            size="sm"
          >
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
}
