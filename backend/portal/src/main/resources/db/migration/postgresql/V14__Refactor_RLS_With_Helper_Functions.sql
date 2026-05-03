-- Define helper functions for accessing session variables
CREATE OR REPLACE FUNCTION current_user_id() RETURNS UUID AS
$$
BEGIN
    RETURN NULLIF(current_setting('app.current_user_id', true), '')::uuid;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION current_household_id() RETURNS UUID AS
$$
BEGIN
    RETURN NULLIF(current_setting('app.current_household_id', true), '')::uuid;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION current_user_email() RETURNS TEXT AS
$$
BEGIN
    RETURN NULLIF(current_setting('app.current_user_email', true), '');
END;
$$ LANGUAGE plpgsql;

-- Refactor existing RLS policies to use the new functions

-- 1. household_members
DROP POLICY IF EXISTS household_isolation_policy ON household_members;
CREATE POLICY household_isolation_policy ON household_members
    USING (household_id = current_household_id() OR user_id = current_user_id());

-- 2. invitations
DROP POLICY IF EXISTS invitation_select_policy ON invitations;
CREATE POLICY invitation_select_policy ON invitations
    FOR SELECT
    USING (household_id = current_household_id() OR email = current_user_email());

DROP POLICY IF EXISTS invitation_insert_policy ON invitations;
CREATE POLICY invitation_insert_policy ON invitations
    FOR INSERT
    WITH CHECK (
    EXISTS (SELECT 1
            FROM household_members
            WHERE household_members.household_id = invitations.household_id
              AND household_members.user_id = current_user_id())
    );

DROP POLICY IF EXISTS invitation_update_policy ON invitations;
CREATE POLICY invitation_update_policy ON invitations
    FOR UPDATE
    USING (household_id = current_household_id() OR email = current_user_email());

-- 3. push_subscriptions
DROP POLICY IF EXISTS push_subscriptions_policy ON push_subscriptions;
CREATE POLICY push_subscriptions_policy ON push_subscriptions
    USING (user_id = current_user_id());

-- 4. spices
DROP POLICY IF EXISTS household_isolation_policy ON spices;
CREATE POLICY household_isolation_policy ON spices
    USING (household_id = current_household_id());

-- 5. pantry_jars
DROP POLICY IF EXISTS household_isolation_policy ON pantry_jars;
CREATE POLICY household_isolation_policy ON pantry_jars
    USING (household_id = current_household_id());
