package uk.co.dajohnston.portal.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.auth0.client.mgmt.ManagementApi;
import com.auth0.client.mgmt.UsersClient;
import com.auth0.client.mgmt.types.GetUserResponseContent;
import com.auth0.client.mgmt.types.UpdateUserRequestContent;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import uk.co.dajohnston.model.UpdateUserProfileRequestDto;
import uk.co.dajohnston.portal.household.HouseholdRole;
import uk.co.dajohnston.portal.household.entity.HouseholdEntity;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberEntity;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberRepository;
import uk.co.dajohnston.portal.user.entity.UserEntity;
import uk.co.dajohnston.portal.user.entity.UserRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private HouseholdMemberRepository householdMemberRepository;
  @Mock private GravatarService gravatarService;
  @Mock private ManagementApi auth0ManagementApi;
  @Mock private UsersClient usersClient;
  @Mock private JwtClaimAccessor jwt;

  @InjectMocks private UserService userService;

  @BeforeEach
  void setUp() {
    lenient().when(auth0ManagementApi.users()).thenReturn(usersClient);
  }

  @Test
  void getCurrentUser_userExists_returnsUserProfile() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserEntity user =
        UserEntity.builder()
            .id(userId)
            .auth0Id("auth0|123")
            .email("test@example.com")
            .useGravatar(false)
            .build();

    GetUserResponseContent auth0User = mock(GetUserResponseContent.class);
    when(auth0User.getGivenName()).thenReturn(Optional.of("John"));
    when(auth0User.getFamilyName()).thenReturn(Optional.of("Doe"));
    when(auth0User.getNickname()).thenReturn(Optional.of("jdoe"));
    when(auth0User.getPicture()).thenReturn(Optional.of("http://example.com/pic.jpg"));

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
    when(usersClient.get("auth0|123")).thenReturn(auth0User);

    UserProfile profile = userService.getCurrentUser(jwt);

    assertThat(profile.id()).isEqualTo(userId);
    assertThat(profile.givenName()).isEqualTo("John");
    assertThat(profile.familyName()).isEqualTo("Doe");
    assertThat(profile.nickname()).isEqualTo("jdoe");
    assertThat(profile.picture()).isEqualTo("http://example.com/pic.jpg");
  }

  @Test
  void getCurrentUser_withHouseholds_returnsUserProfileWithHouseholds() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID householdId = UUID.fromString("456e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();
    HouseholdEntity household = HouseholdEntity.builder().id(householdId).name("My Home").build();
    HouseholdMemberEntity member =
        HouseholdMemberEntity.builder().household(household).role(HouseholdRole.OWNER).build();

    GetUserResponseContent auth0User = mock(GetUserResponseContent.class);
    when(auth0User.getGivenName()).thenReturn(Optional.empty());
    when(auth0User.getFamilyName()).thenReturn(Optional.empty());
    when(auth0User.getNickname()).thenReturn(Optional.empty());
    when(auth0User.getPicture()).thenReturn(Optional.empty());

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdMemberRepository.findByUserId(userId))
        .thenReturn(Collections.singletonList(member));
    when(usersClient.get("auth0|123")).thenReturn(auth0User);

    UserProfile profile = userService.getCurrentUser(jwt);

    assertThat(profile.households()).hasSize(1);
    assertThat(profile.households().getFirst().id()).isEqualTo(householdId);
    assertThat(profile.households().getFirst().name()).isEqualTo("My Home");
    assertThat(profile.households().getFirst().role()).isEqualTo(HouseholdRole.OWNER);
  }

  @Test
  void getCurrentUser_useGravatarTrue_returnsGravatarUrl() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserEntity user =
        UserEntity.builder()
            .id(userId)
            .auth0Id("auth0|123")
            .email("test@example.com")
            .useGravatar(true)
            .build();

    GetUserResponseContent auth0User = mock(GetUserResponseContent.class);
    when(auth0User.getGivenName()).thenReturn(Optional.empty());
    when(auth0User.getFamilyName()).thenReturn(Optional.empty());
    when(auth0User.getNickname()).thenReturn(Optional.empty());

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(householdMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
    when(usersClient.get("auth0|123")).thenReturn(auth0User);
    when(gravatarService.getGravatarUrl("test@example.com"))
        .thenReturn("http://gravatar.com/pic.jpg");

    UserProfile profile = userService.getCurrentUser(jwt);

    assertThat(profile.picture()).isEqualTo("http://gravatar.com/pic.jpg");
  }

  @Test
  void findOrCreateUser_userDoesNotExist_createsUser() {
    when(jwt.getSubject()).thenReturn("auth0|456");
    when(jwt.getClaimAsString("email")).thenReturn("new@example.com");
    when(userRepository.findByAuth0Id("auth0|456")).thenReturn(Optional.empty());
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserEntity user = userService.findOrCreateUser(jwt);

    assertThat(user.getAuth0Id()).isEqualTo("auth0|456");
    assertThat(user.getEmail()).isEqualTo("new@example.com");
    verify(userRepository).save(any(UserEntity.class));
  }

  @Test
  void updateCurrentUser_allFields_updatesAuth0AndLocal() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserEntity user =
        UserEntity.builder()
            .id(userId)
            .auth0Id("auth0|123")
            .email("test@example.com")
            .useGravatar(false)
            .build();

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));

    GetUserResponseContent auth0User = mock(GetUserResponseContent.class);
    when(auth0User.getGivenName()).thenReturn(Optional.of("NewName"));
    when(auth0User.getFamilyName()).thenReturn(Optional.empty());
    when(auth0User.getNickname()).thenReturn(Optional.empty());
    when(usersClient.get("auth0|123")).thenReturn(auth0User);
    when(householdMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

    UpdateUserProfileRequestDto request =
        new UpdateUserProfileRequestDto("NewName", null, null, null, true);

    userService.updateCurrentUser(jwt, request);

    assertThat(user.isUseGravatar()).isTrue();
    verify(userRepository).save(user);
    verify(usersClient).update(eq("auth0|123"), any(UpdateUserRequestContent.class));
  }

  @Test
  void updateCurrentUser_noFields_doesNotUpdateAuth0OrLocal() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserEntity user =
        UserEntity.builder()
            .id(userId)
            .auth0Id("auth0|123")
            .email("test@example.com")
            .useGravatar(false)
            .build();

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));

    GetUserResponseContent auth0User = mock(GetUserResponseContent.class);
    when(auth0User.getGivenName()).thenReturn(Optional.empty());
    when(auth0User.getFamilyName()).thenReturn(Optional.empty());
    when(auth0User.getNickname()).thenReturn(Optional.empty());
    when(auth0User.getPicture()).thenReturn(Optional.empty());
    when(usersClient.get("auth0|123")).thenReturn(auth0User);
    when(householdMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

    UpdateUserProfileRequestDto request =
        new UpdateUserProfileRequestDto(null, null, null, null, null);

    userService.updateCurrentUser(jwt, request);

    verifyNoInteractions(gravatarService);
  }

  @Test
  void updateCurrentUser_onlyNickname_updatesAuth0() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(usersClient.get("auth0|123")).thenReturn(mock(GetUserResponseContent.class));
    when(householdMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

    userService.updateCurrentUser(
        jwt, new UpdateUserProfileRequestDto(null, null, "NewNick", null, null));

    verify(usersClient)
        .update(
            eq("auth0|123"),
            argThat((UpdateUserRequestContent arg) -> arg.getNickname().get().equals("NewNick")));
  }

  @Test
  void updateCurrentUser_onlyGivenName_updatesAuth0() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(usersClient.get("auth0|123")).thenReturn(mock(GetUserResponseContent.class));
    when(householdMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

    userService.updateCurrentUser(
        jwt, new UpdateUserProfileRequestDto("New First", null, null, null, null));

    verify(usersClient)
        .update(
            eq("auth0|123"),
            argThat(
                (UpdateUserRequestContent arg) -> arg.getGivenName().get().equals("New First")));
  }

  @Test
  void updateCurrentUser_onlyFamilyName_updatesAuth0() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(usersClient.get("auth0|123")).thenReturn(mock(GetUserResponseContent.class));
    when(householdMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

    userService.updateCurrentUser(
        jwt, new UpdateUserProfileRequestDto(null, "New Last", null, null, null));

    verify(usersClient)
        .update(
            eq("auth0|123"),
            argThat(
                (UpdateUserRequestContent arg) -> arg.getFamilyName().get().equals("New Last")));
  }

  @Test
  void updateCurrentUser_onlyPicture_updatesAuth0() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(usersClient.get("auth0|123")).thenReturn(mock(GetUserResponseContent.class));
    when(householdMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

    userService.updateCurrentUser(
        jwt, new UpdateUserProfileRequestDto(null, null, null, "https://newpicture", null));

    verify(usersClient)
        .update(
            eq("auth0|123"),
            argThat(
                (UpdateUserRequestContent arg) ->
                    arg.getPicture().get().equals("https://newpicture")));
  }

  @Test
  void updateCurrentUser_onlyUseGravatar_doesNotUpdateAuth0() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UserEntity user = UserEntity.builder().id(userId).auth0Id("auth0|123").build();

    when(jwt.getSubject()).thenReturn("auth0|123");
    when(userRepository.findByAuth0Id("auth0|123")).thenReturn(Optional.of(user));
    when(usersClient.get("auth0|123")).thenReturn(mock(GetUserResponseContent.class));
    when(householdMemberRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

    userService.updateCurrentUser(
        jwt, new UpdateUserProfileRequestDto(null, null, null, null, true));

    verify(usersClient, never()).update(anyString(), any(UpdateUserRequestContent.class));
  }
}
