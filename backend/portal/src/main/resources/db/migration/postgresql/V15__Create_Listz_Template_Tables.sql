CREATE TABLE listz_items
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id     UUID NOT NULL REFERENCES households (id),
    name             TEXT NOT NULL,
    default_category TEXT,
    UNIQUE (household_id, name)
);

ALTER TABLE listz_items
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE listz_items
    FORCE ROW LEVEL SECURITY;

CREATE POLICY household_isolation_policy ON listz_items
    USING (household_id = current_household_id());

CREATE TABLE listz_templates
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    household_id UUID        NOT NULL REFERENCES households (id),
    name         TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at   TIMESTAMPTZ
);

ALTER TABLE listz_templates
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE listz_templates
    FORCE ROW LEVEL SECURITY;

CREATE POLICY household_isolation_policy ON listz_templates
    USING (household_id = current_household_id());

CREATE TABLE listz_template_items
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id        UUID    NOT NULL REFERENCES households (id),
    template_id         UUID    NOT NULL REFERENCES listz_templates (id),
    item_id             UUID    NOT NULL REFERENCES listz_items (id),
    quantity_rule_type  TEXT    NOT NULL, -- 'FIXED', 'PER_DAY'
    quantity_rule_value NUMERIC NOT NULL DEFAULT 1,
    category_override   TEXT,
    UNIQUE (template_id, item_id)
);

ALTER TABLE listz_template_items
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE listz_template_items
    FORCE ROW LEVEL SECURITY;

CREATE POLICY household_isolation_policy ON listz_template_items
    USING (household_id = current_household_id());
