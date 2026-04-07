CREATE TABLE invitations
(
    id           UUID PRIMARY KEY                  DEFAULT gen_random_uuid(),
    household_id UUID                     NOT NULL REFERENCES households (id) ON DELETE CASCADE,
    email        VARCHAR(255)             NOT NULL,
    role         VARCHAR(50)              NOT NULL,
    status       VARCHAR(50)              NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (household_id, email)
);
