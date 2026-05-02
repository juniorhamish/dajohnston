package uk.co.dajohnston.security.context;

import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TenantContext {

  private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
  private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();
  private static final ThreadLocal<String> USER_EMAIL = new ThreadLocal<>();

  public static UUID getTenantId() {
    return TENANT_ID.get();
  }

  public static void setTenantId(UUID tenantId) {
    TENANT_ID.set(tenantId);
  }

  public static UUID getUserId() {
    return USER_ID.get();
  }

  public static void setUserId(UUID userId) {
    USER_ID.set(userId);
  }

  public static String getUserEmail() {
    return USER_EMAIL.get();
  }

  public static void setUserEmail(String email) {
    USER_EMAIL.set(email);
  }

  public static void clear() {
    TENANT_ID.remove();
    USER_ID.remove();
    USER_EMAIL.remove();
  }
}
