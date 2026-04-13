package uk.co.dajohnston.portal.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberId;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberRepository;
import uk.co.dajohnston.portal.user.UserService;
import uk.co.dajohnston.portal.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
class TenantInterceptorTest {

  @Mock private HouseholdMemberRepository householdMemberRepository;
  @Mock private UserService userService;

  @InjectMocks private TenantInterceptor tenantInterceptor;

  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @BeforeEach
  void setUp() {
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();
  }

  @AfterEach
  void tearDown() {
    TenantContext.clear();
    SecurityContextHolder.clearContext();
  }

  @Test
  void preHandle_setsUserIdWhenAuthenticated() {
    UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    Jwt jwt = createJwt();
    setAuthentication(jwt);
    UserEntity user = UserEntity.builder().id(userId).build();
    when(userService.findOrCreateUser(jwt)).thenReturn(user);

    boolean result = tenantInterceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(TenantContext.getUserId()).isEqualTo(userId);
    assertThat(TenantContext.getTenantId()).isNull();
  }

  @Test
  void preHandle_setsTenantIdWhenHouseholdIdHeaderPresent() {
    UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    UUID householdId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    Jwt jwt = createJwt();
    setAuthentication(jwt);
    UserEntity user = UserEntity.builder().id(userId).build();
    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(householdMemberRepository.existsById(new HouseholdMemberId(householdId, userId)))
        .thenReturn(true);

    request.addHeader("X-Household-Id", householdId.toString());

    boolean result = tenantInterceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(TenantContext.getUserId()).isEqualTo(userId);
    assertThat(TenantContext.getTenantId()).isEqualTo(householdId);
  }

  @Test
  void preHandle_throwsAccessDeniedWhenUserNotMemberOfHousehold() {
    UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    UUID householdId = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    Jwt jwt = createJwt();
    setAuthentication(jwt);
    UserEntity user = UserEntity.builder().id(userId).build();
    when(userService.findOrCreateUser(jwt)).thenReturn(user);
    when(householdMemberRepository.existsById(new HouseholdMemberId(householdId, userId)))
        .thenReturn(false);

    request.addHeader("X-Household-Id", householdId.toString());

    Object handler = new Object();
    assertThatThrownBy(() -> tenantInterceptor.preHandle(request, response, handler))
        .isInstanceOf(AccessDeniedException.class)
        .hasMessage("User is not a member of the requested household");
  }

  @Test
  void preHandle_doesNotSetContextWhenNotAuthenticated() {
    boolean result = tenantInterceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(TenantContext.getUserId()).isNull();
    assertThat(TenantContext.getTenantId()).isNull();
    verify(userService, never()).findOrCreateUser(any());
  }

  @Test
  void preHandle_doesNotSetContextWhenNotUsingJwtAuthentication() {
    SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("", ""));
    boolean result = tenantInterceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(TenantContext.getUserId()).isNull();
    assertThat(TenantContext.getTenantId()).isNull();
    verify(userService, never()).findOrCreateUser(any());
  }

  @Test
  void preHandle_doesNotSetTenantIdWhenHouseholdIdHeaderIsBlank() {
    UUID userId = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    Jwt jwt = createJwt();
    setAuthentication(jwt);
    UserEntity user = UserEntity.builder().id(userId).build();
    when(userService.findOrCreateUser(jwt)).thenReturn(user);

    request.addHeader("X-Household-Id", "  ");

    boolean result = tenantInterceptor.preHandle(request, response, new Object());

    assertThat(result).isTrue();
    assertThat(TenantContext.getUserId()).isEqualTo(userId);
    assertThat(TenantContext.getTenantId()).isNull();
    verify(householdMemberRepository, never()).existsById(any());
  }

  @Test
  void afterCompletion_clearsTenantContext() {
    TenantContext.setUserId(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"));
    TenantContext.setTenantId(UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901"));

    tenantInterceptor.afterCompletion(request, response, new Object(), null);

    assertThat(TenantContext.getUserId()).isNull();
    assertThat(TenantContext.getTenantId()).isNull();
  }

  private Jwt createJwt() {
    return Jwt.withTokenValue("token").header("alg", "RS256").claim("sub", "auth0|123456").build();
  }

  private void setAuthentication(Jwt jwt) {
    SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
  }
}
