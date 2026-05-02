CREATE TABLE spices
(
    id           UUID PRIMARY KEY,
    household_id UUID NOT NULL,
    name         TEXT NOT NULL,
    UNIQUE (household_id, name)
);

ALTER TABLE spices
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE spices
    FORCE ROW LEVEL SECURITY;

CREATE POLICY household_isolation_policy ON spices
    USING (household_id = NULLIF(current_setting('app.current_household_id', true), '')::uuid);

CREATE TABLE pantry_jars
(
    id           UUID PRIMARY KEY,
    household_id UUID    NOT NULL,
    spice_id     UUID    NOT NULL REFERENCES spices (id),
    quantity     INTEGER NOT NULL CHECK (quantity >= 0 AND quantity <= 100)
);

ALTER TABLE pantry_jars
    ENABLE ROW LEVEL SECURITY;
ALTER TABLE pantry_jars
    FORCE ROW LEVEL SECURITY;

CREATE POLICY household_isolation_policy ON pantry_jars
    USING (household_id = NULLIF(current_setting('app.current_household_id', true), '')::uuid);
