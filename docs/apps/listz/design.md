# Listz Design Document

## Overview

Listz is a household-centric checklist application. It allows users to define reusable checklist
templates and then create specific instances (checklists in use) for events like trips or races.

Key features:

- **Checklist Templates**: Shared within a household. Simple lists of items with quantity rules.
- **Master Item Catalog**: Auto-completion and deduplication of items across the household.
- **Composition**: Instances can be composed of multiple templates.
- **Dynamic Quantities**: Support for rules like "per day" based on instance duration.
- **Live Sync**: Changes to templates reflect in active instances.
- **Sharing**: Instances can be shared with other household members.

## User Flows

### Admin: Managing Templates

1. **Create Template**: User names a template (e.g., "Standard Packing List").
2. **Add Item**: User types an item name. Auto-complete suggests existing items in the household.
3. **Define Rule**: User sets a quantity rule:
    - **Fixed**: e.g., 1 Passport.
    - **Per Day**: e.g., 1 pair of socks per day.
4. **Set Category**: Optional category for grouping (e.g., "Toiletries", "Clothing").

### User: Using a Checklist (Instance)

1. **Create Instance**: User names an instance (e.g., "Prague Half Marathon May 2026").
2. **Select Templates**: User selects one or more templates to include (e.g., "Standard Packing" + "
   Running Race").
3. **Set Parameters**: User sets the duration (e.g., 3 days).
4. **Review Items**: The app generates a combined list of items.
    - Quantities are calculated based on rules (e.g., 3 pairs of socks).
    - Items are grouped by Template and/or Category.
5. **Pack**: User checks off items as they are packed.
6. **Override**: User can manually override a quantity for this specific instance (e.g., "I need 5
   pairs of socks this time").
7. **Share**: User shares the instance with their partner in the same household.

## API Specifications

### Templates

- `GET /api/v1/listz/templates`: List all templates in the current household.
- `POST /api/v1/listz/templates`: Create a new template.
- `GET /api/v1/listz/templates/{id}`: Get template details including items.
- `PUT /api/v1/listz/templates/{id}`: Update template name.
- `DELETE /api/v1/listz/templates/{id}`: Soft-delete template.
- `POST /api/v1/listz/templates/{id}/items`: Add an item to a template.
- `PUT /api/v1/listz/template-items/{itemId}`: Update quantity rule or category for a template item.
- `DELETE /api/v1/listz/template-items/{itemId}`: Remove item from template.

### Items (Catalog)

- `GET /api/v1/listz/items/search?q=...`: Search for items in the household for auto-complete.

### Instances

- `GET /api/v1/listz/instances`: List instances owned by or shared with the user.
- `POST /api/v1/listz/instances`: Create a new instance (name, days, initial templates).
- `GET /api/v1/listz/instances/{id}`: Get instance details and the dynamic list of items.
- `PATCH /api/v1/listz/instances/{id}`: Update instance parameters (name, days, templates).
- `PUT /api/v1/listz/instances/{id}/items/{itemId}/check`: Toggle checked status.
- `PUT /api/v1/listz/instances/{id}/items/{itemId}/quantity`: Override quantity.
- `POST /api/v1/listz/instances/{id}/share`: Share with another user.

## Technical Details

### Database Schema

```sql
-- Master Catalog
CREATE TABLE listz_items
(
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    household_id     UUID NOT NULL REFERENCES households (id),
    name             TEXT NOT NULL,
    default_category TEXT,
    UNIQUE (household_id, name)
);

-- Templates
CREATE TABLE listz_templates
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    household_id UUID        NOT NULL REFERENCES households (id),
    name         TEXT        NOT NULL,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at   TIMESTAMPTZ
);

CREATE TABLE listz_template_items
(
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    template_id         UUID    NOT NULL REFERENCES listz_templates (id),
    item_id             UUID    NOT NULL REFERENCES listz_items (id),
    quantity_rule_type  TEXT    NOT NULL, -- 'FIXED', 'PER_DAY'
    quantity_rule_value NUMERIC NOT NULL DEFAULT 1,
    category_override   TEXT,
    UNIQUE (template_id, item_id)
);

-- Instances
CREATE TABLE listz_instances
(
    id            UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    household_id  UUID        NOT NULL REFERENCES households (id),
    owner_user_id TEXT        NOT NULL, -- Auth0 sub
    name          TEXT        NOT NULL,
    duration_days INTEGER     NOT NULL DEFAULT 1,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE listz_instance_templates
(
    instance_id UUID NOT NULL REFERENCES listz_instances (id),
    template_id UUID NOT NULL REFERENCES listz_templates (id),
    PRIMARY KEY (instance_id, template_id)
);

-- Instance Item State (Overrides and Check status)
-- This table stores state for items derived from templates.
CREATE TABLE listz_instance_item_states
(
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instance_id       UUID    NOT NULL REFERENCES listz_instances (id),
    template_id       UUID    NOT NULL REFERENCES listz_templates (id),
    item_id           UUID    NOT NULL REFERENCES listz_items (id),
    quantity_override NUMERIC,
    is_checked        BOOLEAN NOT NULL DEFAULT false,
    UNIQUE (instance_id, template_id, item_id)
);

-- Sharing
CREATE TABLE listz_instance_shares
(
    instance_id UUID NOT NULL REFERENCES listz_instances (id),
    user_id     TEXT NOT NULL, -- Auth0 sub
    PRIMARY KEY (instance_id, user_id)
);
```

### Row Level Security (RLS)

1. **listz_items, listz_templates, listz_template_items**:
    - `SELECT/INSERT/UPDATE/DELETE`: `household_id = current_household_id()`
2. **listz_instances**:
    - `SELECT`:
      `owner_user_id = current_user_id() OR id IN (SELECT instance_id FROM listz_instance_shares WHERE user_id = current_user_id())`
    - `INSERT`: Must belong to a household where `current_user_id()` is a member.
3. **listz_instance_item_states**:
    - Linked to instance visibility.

### Logic for "Live" List

The items for an instance will be fetched via a query that joins `listz_instance_templates`,
`listz_template_items`, and `listz_items`, then left-joins `listz_instance_item_states`.

- If an item is in the template but not in `item_states`, it's shown as unchecked with the
  calculated quantity.
- If a new item is added to the template, it automatically appears in the join.
- If an item is removed from the template, it disappears from the join (unless we want to keep it as
  an orphan, which we could do by also selecting from `item_states`).

### Calculated Quantity Logic

```sql
CASE 
    WHEN ti.quantity_rule_type = 'FIXED' THEN ti.quantity_rule_value
    WHEN ti.quantity_rule_type = 'PER_DAY' THEN ti.quantity_rule_value * i.duration_days
    ELSE ti.quantity_rule_value
END
```

### Implementation Plan

#### Phase 1: Backend Foundation & Template Management

1. **Database Migration (V15)**: Create `listz_items`, `listz_templates`, and `listz_template_items`
   tables with RLS enabled.
2. **Item Catalog**:
    * Implement `ListzItemEntity`, `ListzItemRepository`.
    * Create `ListzItemService` for catalog search and deduplication logic (ensure `household_id`
      isolation).
3. **Template Management**:
    * Implement `ListzTemplateEntity`, `ListzTemplateItemEntity`, and their repositories.
    * Create `ListzTemplateService` and `ListzTemplateController` for CRUD operations on templates
      and their items.
4. **Verification**: Write unit tests for services and integration tests for template/catalog
   endpoints.

#### Phase 2: Checklist Instances & Dynamic Logic

1. **Database Migration (V16)**: Create `listz_instances`, `listz_instance_templates`,
   `listz_instance_item_states`, and `listz_instance_shares` tables with RLS.
2. **Instance Core**:
    * Implement `ListzInstanceEntity`, `ListzInstanceItemStateEntity`, `ListzInstanceShareEntity`.
    * Implement `ListzInstanceService` with the dynamic quantity calculation logic and template
      composition.
3. **Instance API**:
    * Create `ListzInstanceController` for instance CRUD, item toggling, and quantity overrides.
    * Implement sharing logic (`POST /share`).
4. **Verification**: Write integration tests covering instance creation from multiple templates
   and "live" item list generation.

#### Phase 3: Frontend - Templates & Catalog

1. **API Client Generation**: Run `hey-api` to generate Next.js clients from `listz.yaml`.
2. **Template Management UI**:
    * Create template list and creation screens.
    * Implement template editor with item auto-complete (fetching from catalog).
3. **Verification**: Vitest for UI components and basic interaction tests.

#### Phase 4: Frontend - Active Checklists

1. **Instance Management UI**:
    * Create instance list and "New Instance" wizard (multi-template selection).
2. **Checklist UI**:
    * Implement the active checklist view with grouping by category and template.
    * Add indicators for items added to templates after instance creation.
3. **Sharing UI**: Implement share dialog and user selection within household.
4. **Verification**: End-to-end flow tests for creating, sharing, and using a checklist.

#### Phase 5: Registration & Final Polish

1. **App Registration (V17)**: Add Listz to the `apps` table.
2. **RLS Audit**: Final security review of RLS policies.
3. **Deployment**: Update PWA manifest and check mobile responsiveness.
