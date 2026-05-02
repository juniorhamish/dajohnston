package uk.co.dajohnston.portal.config;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    if (TransactionSynchronizationManager.isActualTransactionActive()) {
      if (TransactionSynchronizationManager.getResource(this) != null) {
        return;
      }
      TransactionSynchronizationManager.bindResource(this, Boolean.TRUE);
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
              TransactionSynchronizationManager.unbindResourceIfPossible(TenantSessionAspect.this);
            }
          });
    }

    long start = System.currentTimeMillis();
    UUID userId = TenantContext.getUserId();
    String email = TenantContext.getUserEmail();
    UUID tenantId = TenantContext.getTenantId();

    log.debug(
        "Setting PostgreSQL session variables: user_id={}, email={}, household_id={}",
        userId,
        email,
        tenantId);

    jdbcTemplate.queryForList(
        "SELECT set_config('app.current_user_id', ?, true), "
            + "set_config('app.current_user_email', ?, true), "
            + "set_config('app.current_household_id', ?, true)",
        userId != null ? userId.toString() : "",
        email != null ? email : "",
        tenantId != null ? tenantId.toString() : "");

    log.debug(
        "Completed setting tenant session variables in [{}ms]", System.currentTimeMillis() - start);
  }
}
