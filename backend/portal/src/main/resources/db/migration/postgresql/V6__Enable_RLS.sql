-- Enable RLS on tables with household_id
ALTER TABLE household_members
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE household_members
    FORCE ROW LEVEL SECURITY;

ALTER TABLE invitations
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE invitations
    FORCE ROW LEVEL SECURITY;

-- Create policies based on the session variables app.current_household_id and app.current_user_id
-- For household_members, we allow users to see their own records (to list their households)
-- and see everyone in the current household context.
CREATE POLICY household_isolation_policy ON household_members
    USING (
    (household_id = NULLIF(current_setting('app.current_household_id', true), '')::uuid)
        OR
    (user_id = NULLIF(current_setting('app.current_user_id', true), '')::uuid)
    );

-- For invitations, we allow seeing them if they are in the current household context.
-- Future work could allow seeing them by email.
CREATE POLICY household_isolation_policy ON invitations
    USING (household_id = NULLIF(current_setting('app.current_household_id', true), '')::uuid);
