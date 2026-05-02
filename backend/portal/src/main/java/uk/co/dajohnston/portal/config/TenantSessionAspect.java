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
    long start = System.currentTimeMillis();
    UUID userId = TenantContext.getUserId();
    if (userId != null) {
      log.debug("Setting PostgreSQL session variable app.current_user_id to {}", userId);
      jdbcTemplate.queryForObject(
          "SELECT set_config('app.current_user_id', ?, true)", String.class, userId.toString());
    } else {
      log.debug("No user context found, clearing PostgreSQL session variable");
      jdbcTemplate.queryForObject(
          "SELECT set_config('app.current_user_id', '', true)", String.class);
    }

    String email = TenantContext.getUserEmail();
    if (email != null) {
      log.debug("Setting PostgreSQL session variable app.current_user_email to {}", email);
      jdbcTemplate.queryForObject(
          "SELECT set_config('app.current_user_email', ?, true)", String.class, email);
    } else {
      jdbcTemplate.queryForObject(
          "SELECT set_config('app.current_user_email', '', true)", String.class);
    }

    UUID tenantId = TenantContext.getTenantId();
    if (tenantId != null) {
      log.debug("Setting PostgreSQL session variable app.current_household_id to {}", tenantId);
      jdbcTemplate.queryForObject(
          "SELECT set_config('app.current_household_id', ?, true)",
          String.class,
          tenantId.toString());
    } else {
      log.debug("No tenant context found, clearing PostgreSQL session variable");
      jdbcTemplate.queryForObject(
          "SELECT set_config('app.current_household_id', '', true)", String.class);
    }
    log.debug(
        "Completed setting tenant session variables in [{}ms]", System.currentTimeMillis() - start);
  }
}
