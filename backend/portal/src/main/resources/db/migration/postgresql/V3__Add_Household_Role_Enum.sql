CREATE TYPE household_role AS ENUM ('OWNER', 'MEMBER');

ALTER TABLE household_members
    ALTER COLUMN role DROP DEFAULT;
ALTER TABLE household_members
    ALTER COLUMN role TYPE household_role USING role::household_role;
ALTER TABLE household_members
    ALTER COLUMN role SET DEFAULT 'MEMBER'::household_role;

ALTER TABLE invitations
    ALTER COLUMN role TYPE household_role USING role::household_role;
