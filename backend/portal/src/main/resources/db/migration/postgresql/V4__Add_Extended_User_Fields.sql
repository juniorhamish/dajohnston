ALTER TABLE users
    ADD COLUMN use_gravatar BOOLEAN DEFAULT FALSE,
    DROP COLUMN display_name;
