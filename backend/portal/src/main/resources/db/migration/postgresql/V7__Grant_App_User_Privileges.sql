-- Grant DML privileges to the application user so it can read/write data
-- but does not own tables, making it fully subject to RLS policies.
DO
$$
    BEGIN
        IF EXISTS (SELECT FROM pg_roles WHERE rolname = 'portal_app') THEN
            GRANT USAGE ON SCHEMA public TO portal_app;
            GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO portal_app;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO portal_app;
            GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO portal_app;
            ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO portal_app;
        END IF;
    END
$$;
