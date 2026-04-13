package uk.co.dajohnston.portal.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberId;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberRepository;
import uk.co.dajohnston.portal.user.UserService;
import uk.co.dajohnston.portal.user.entity.UserEntity;

@Component
@RequiredArgsConstructor
@NullMarked
public class TenantInterceptor implements HandlerInterceptor {

  private static final String HOUSEHOLD_ID_HEADER = "X-Household-Id";
  private final HouseholdMemberRepository householdMemberRepository;
  private final UserService userService;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    var authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
      UserEntity user = userService.findOrCreateUser(jwt);
      TenantContext.setUserId(user.getId());
      TenantContext.setUserEmail(user.getEmail());

      String householdIdHeader = request.getHeader(HOUSEHOLD_ID_HEADER);
      if (householdIdHeader != null && !householdIdHeader.isBlank()) {
        var householdId = UUID.fromString(householdIdHeader);
        if (!householdMemberRepository.existsById(
            new HouseholdMemberId(householdId, user.getId()))) {
          throw new AccessDeniedException("User is not a member of the requested household");
        }
        TenantContext.setTenantId(householdId);
      }
    }
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request,
      HttpServletResponse response,
      Object handler,
      @Nullable Exception ex) {
    TenantContext.clear();
  }
}
