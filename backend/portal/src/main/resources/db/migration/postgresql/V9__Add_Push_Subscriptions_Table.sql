CREATE TABLE push_subscriptions
(
    id              UUID PRIMARY KEY         DEFAULT gen_random_uuid(),
    user_id         UUID                                               NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    endpoint        TEXT                                               NOT NULL,
    p256dh          VARCHAR(255)                                       NOT NULL,
    auth            VARCHAR(255)                                       NOT NULL,
    expiration_time BIGINT,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE INDEX idx_push_subscriptions_user_id ON push_subscriptions (user_id);
CREATE INDEX idx_push_subscriptions_endpoint ON push_subscriptions (endpoint);

ALTER TABLE push_subscriptions
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE push_subscriptions
    FORCE ROW LEVEL SECURITY;

CREATE POLICY push_subscriptions_policy ON push_subscriptions
    USING (user_id = NULLIF(current_setting('app.current_user_id', true), '')::uuid);
