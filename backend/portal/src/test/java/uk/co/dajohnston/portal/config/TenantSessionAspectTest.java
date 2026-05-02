package uk.co.dajohnston.portal.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import uk.co.dajohnston.security.context.TenantContext;

@ExtendWith(MockitoExtension.class)
class TenantSessionAspectTest {

  @Mock private JdbcTemplate jdbcTemplate;

  private TenantSessionAspect tenantSessionAspect;

  @BeforeEach
  void setUp() {
    tenantSessionAspect = new TenantSessionAspect(jdbcTemplate);
    // Standard lenient stubbing for the batched call
    lenient().when(jdbcTemplate.queryForList(anyString(), any(), any(), any())).thenReturn(null);
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
  }

  @Test
  void setTenantSessionVariable_setsUserIdWhenPresent() {
    UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    TenantContext.setUserId(userId);

    tenantSessionAspect.setTenantSessionVariable();

    verify(jdbcTemplate)
        .queryForList(
            "SELECT set_config('app.current_user_id', ?, true), "
                + "set_config('app.current_user_email', ?, true), "
                + "set_config('app.current_household_id', ?, true)",
            userId.toString(),
            "",
            "");
  }

  @Test
  void setTenantSessionVariable_resetUserIdWhenNull() {
    tenantSessionAspect.setTenantSessionVariable();

    verify(jdbcTemplate)
        .queryForList(
            "SELECT set_config('app.current_user_id', ?, true), "
                + "set_config('app.current_user_email', ?, true), "
                + "set_config('app.current_household_id', ?, true)",
            "",
            "",
            "");
  }

  @Test
  void setTenantSessionVariable_setsTenantIdWhenPresent() {
    UUID tenantId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    TenantContext.setTenantId(tenantId);

    tenantSessionAspect.setTenantSessionVariable();

    verify(jdbcTemplate)
        .queryForList(
            "SELECT set_config('app.current_user_id', ?, true), "
                + "set_config('app.current_user_email', ?, true), "
                + "set_config('app.current_household_id', ?, true)",
            "",
            "",
            tenantId.toString());
  }

  @Test
  void setTenantSessionVariable_setsBothWhenPresent() {
    UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    UUID tenantId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    TenantContext.setUserId(userId);
    TenantContext.setTenantId(tenantId);

    tenantSessionAspect.setTenantSessionVariable();

    verify(jdbcTemplate)
        .queryForList(
            "SELECT set_config('app.current_user_id', ?, true), "
                + "set_config('app.current_user_email', ?, true), "
                + "set_config('app.current_household_id', ?, true)",
            userId.toString(),
            "",
            tenantId.toString());
  }

  @Test
  void setTenantSessionVariable_setsEmailWhenPresent() {
    UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    String email = "test@example.com";
    TenantContext.setUserId(userId);
    TenantContext.setUserEmail(email);

    tenantSessionAspect.setTenantSessionVariable();

    verify(jdbcTemplate)
        .queryForList(
            "SELECT set_config('app.current_user_id', ?, true), "
                + "set_config('app.current_user_email', ?, true), "
                + "set_config('app.current_household_id', ?, true)",
            userId.toString(),
            email,
            "");
  }
}
