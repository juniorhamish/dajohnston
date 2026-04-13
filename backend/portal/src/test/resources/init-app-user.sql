-- Create a non-owner application user for integration tests.
-- This user is subject to Row-Level Security policies because it does NOT own the tables.
-- Privilege grants (USAGE, DML on tables/sequences) are handled by Flyway migration V7.
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'portal_app') THEN
            CREATE ROLE portal_app WITH LOGIN PASSWORD 'portal_app_password';
        END IF;
    END
$$;
