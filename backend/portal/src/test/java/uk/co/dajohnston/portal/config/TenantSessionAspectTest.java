package uk.co.dajohnston.portal.config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class TenantSessionAspectTest {

  @Mock private JdbcTemplate jdbcTemplate;

  @InjectMocks private TenantSessionAspect tenantSessionAspect;

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void setTenantSessionVariable_setsUserIdWhenPresent() {
    UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    TenantContext.setUserId(userId);

    when(jdbcTemplate.queryForObject(
            "SELECT set_config('app.current_user_id', ?, false)", String.class, userId.toString()))
        .thenReturn(userId.toString());

    tenantSessionAspect.setTenantSessionVariable();

    verify(jdbcTemplate)
        .queryForObject(
            "SELECT set_config('app.current_user_id', ?, false)", String.class, userId.toString());
    verify(jdbcTemplate).execute("RESET app.current_household_id");
  }

  @Test
  void setTenantSessionVariable_resetUserIdWhenNull() {
    tenantSessionAspect.setTenantSessionVariable();

    verify(jdbcTemplate).execute("RESET app.current_user_id");
    verify(jdbcTemplate).execute("RESET app.current_household_id");
  }

  @Test
  void setTenantSessionVariable_setsTenantIdWhenPresent() {
    UUID tenantId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    TenantContext.setTenantId(tenantId);

    when(jdbcTemplate.queryForObject(
            "SELECT set_config('app.current_household_id', ?, false)",
            String.class,
            tenantId.toString()))
        .thenReturn(tenantId.toString());

    tenantSessionAspect.setTenantSessionVariable();

    verify(jdbcTemplate).execute("RESET app.current_user_id");
    verify(jdbcTemplate)
        .queryForObject(
            "SELECT set_config('app.current_household_id', ?, false)",
            String.class,
            tenantId.toString());
  }

  @Test
  void setTenantSessionVariable_setsBothWhenPresent() {
    UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    UUID tenantId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    TenantContext.setUserId(userId);
    TenantContext.setTenantId(tenantId);

    when(jdbcTemplate.queryForObject(
            "SELECT set_config('app.current_user_id', ?, false)", String.class, userId.toString()))
        .thenReturn(userId.toString());
    when(jdbcTemplate.queryForObject(
            "SELECT set_config('app.current_household_id', ?, false)",
            String.class,
            tenantId.toString()))
        .thenReturn(tenantId.toString());

    tenantSessionAspect.setTenantSessionVariable();

    verify(jdbcTemplate)
        .queryForObject(
            "SELECT set_config('app.current_user_id', ?, false)", String.class, userId.toString());
    verify(jdbcTemplate)
        .queryForObject(
            "SELECT set_config('app.current_household_id', ?, false)",
            String.class,
            tenantId.toString());
  }
}
