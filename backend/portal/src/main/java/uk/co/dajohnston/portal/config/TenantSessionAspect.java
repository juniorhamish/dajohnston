package uk.co.dajohnston.portal.config;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order
@RequiredArgsConstructor
@Slf4j
public class TenantSessionAspect {

  private final JdbcTemplate jdbcTemplate;

  @Before("@within(org.springframework.transaction.annotation.Transactional)")
  public void setTenantSessionVariable() {
    UUID userId = TenantContext.getUserId();
    if (userId != null) {
      log.debug("Setting PostgreSQL session variable app.current_user_id to {}", userId);
      jdbcTemplate.queryForObject(
          "SELECT set_config('app.current_user_id', ?, false)", String.class, userId.toString());
    } else {
      jdbcTemplate.execute("RESET app.current_user_id");
    }

    UUID tenantId = TenantContext.getTenantId();
    if (tenantId != null) {
      log.debug("Setting PostgreSQL session variable app.current_household_id to {}", tenantId);
      jdbcTemplate.queryForObject(
          "SELECT set_config('app.current_household_id', ?, false)",
          String.class,
          tenantId.toString());
    } else {
      log.debug("No tenant context found, clearing PostgreSQL session variable");
      jdbcTemplate.execute("RESET app.current_household_id");
    }
  }
}
