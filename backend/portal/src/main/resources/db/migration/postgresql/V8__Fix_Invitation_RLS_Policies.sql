-- Drop the overly restrictive ALL policy that blocks INSERT and join operations
DROP POLICY household_isolation_policy ON invitations;

-- SELECT: allow viewing invitations in the current household context,
-- or invitations addressed to the current user's email (needed for joining)
CREATE POLICY invitation_select_policy ON invitations
    FOR SELECT
    USING (
    household_id = NULLIF(current_setting('app.current_household_id', true), '')::uuid
        OR email = NULLIF(current_setting('app.current_user_email', true), '')
    );

-- INSERT: allow creating invitations only if the current user is a member of the target household
CREATE POLICY invitation_insert_policy ON invitations
    FOR INSERT
    WITH CHECK (
    EXISTS (SELECT 1
            FROM household_members
            WHERE household_members.household_id = invitations.household_id
              AND household_members.user_id =
                  NULLIF(current_setting('app.current_user_id', true), '')::uuid)
    );

-- UPDATE: allow updating invitations in the current household context,
-- or invitations addressed to the current user's email (needed for accepting)
CREATE POLICY invitation_update_policy ON invitations
    FOR UPDATE
    USING (
    household_id = NULLIF(current_setting('app.current_household_id', true), '')::uuid
        OR email = NULLIF(current_setting('app.current_user_email', true), '')
    );
