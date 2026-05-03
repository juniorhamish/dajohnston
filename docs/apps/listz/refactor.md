# Refactor Plan for Listz Integration

This document outlines the changes needed in the shared portal infrastructure to better support the
Listz application and future sub-apps.

## Database Helper Functions

The current RLS policies use a long-winded expression to access session variables. We should
introduce helper functions to improve readability and reduce duplication in future migrations.

### Proposed SQL Functions

```sql
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
```

## Update Existing RLS Policies

Once these functions are defined, we should consider refactoring existing RLS policies (in
`household_members`, `invitations`, etc.) to use them. This is not strictly required for Listz but
will improve code quality.

## Global Master Catalog

The Listz design introduces `listz_items` as a master catalog per household. We should ensure that
the `TenantContext` and RLS are properly enforced so that auto-completion from this catalog does not
leak data between households. The current `TenantInterceptor` already handles `X-Household-Id`,
which is sufficient.
