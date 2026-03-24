CREATE TABLE households
(
    id         UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    name       VARCHAR(255)                                       NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE users
(
    id            UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    auth0_id      VARCHAR(255) UNIQUE                                NOT NULL,
    email         VARCHAR(255) UNIQUE                                NOT NULL,
    display_name  VARCHAR(255),
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE household_members
(
    household_id UUID        NOT NULL REFERENCES households (id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role         VARCHAR(50) NOT NULL     DEFAULT 'MEMBER',
    joined_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    PRIMARY KEY (household_id, user_id)
);

CREATE TABLE apps
(
    id          VARCHAR(50) PRIMARY KEY,
    name        VARCHAR(255)                                       NOT NULL,
    description TEXT,
    icon        VARCHAR(255),
    url         VARCHAR(255)                                       NOT NULL,
    is_active   BOOLEAN                  DEFAULT TRUE              NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_users_auth0_id ON users (auth0_id);
