"use client";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import type { UserProfile } from "@/generated";
import { UpdateUserProfileForm } from "./update-user-profile-form";

interface UserProfileClientProps {
  userProfile: UserProfile;
}

export function UserProfileClient({
  userProfile,
}: Readonly<UserProfileClientProps>) {
  const [isEditing, setIsEditing] = useState(false);

  if (isEditing) {
    return (
      <UpdateUserProfileForm
        userProfile={userProfile}
        onCancel={() => setIsEditing(false)}
      />
    );
  }

  return (
    <Button
      variant="outline"
      size="sm"
      onClick={() => setIsEditing(true)}
      className="mt-2"
    >
      Edit Profile
    </Button>
  );
}
