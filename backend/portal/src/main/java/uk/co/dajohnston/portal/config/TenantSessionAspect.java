package uk.co.dajohnston.portal.config;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import uk.co.dajohnston.security.context.TenantContext;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSessionAspect {

  private final JdbcTemplate jdbcTemplate;

  @Before(
      "@within(org.springframework.transaction.annotation.Transactional) "
          + "|| @annotation(org.springframework.transaction.annotation.Transactional)")
  public void setTenantSessionVariable() {
    UUID userId = TenantContext.getUserId();
    String email = TenantContext.getUserEmail();
    UUID tenantId = TenantContext.getTenantId();

    log.debug(
        "Setting PostgreSQL session variables: user_id={}, email={}, household_id={}",
        userId,
        email,
        tenantId);

    var userIdString = "";
    if (userId != null) {
      userIdString = userId.toString();
    }
    if (email == null) {
      email = "";
    }
    var tenantIdString = "";
    if (tenantId != null) {
      tenantIdString = tenantId.toString();
    }

    jdbcTemplate.queryForList(
        "SELECT set_config('app.current_user_id', ?, true), "
            + "set_config('app.current_user_email', ?, true), "
            + "set_config('app.current_household_id', ?, true)",
        userIdString,
        email,
        tenantIdString);
  }
}
