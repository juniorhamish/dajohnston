package uk.co.dajohnston.portal.user;

import com.auth0.client.mgmt.ManagementApi;
import com.auth0.client.mgmt.types.GetUserResponseContent;
import com.auth0.client.mgmt.types.UpdateUserRequestContent;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.dajohnston.model.UpdateUserProfileRequestDto;
import uk.co.dajohnston.portal.household.Household;
import uk.co.dajohnston.portal.household.entity.HouseholdMemberRepository;
import uk.co.dajohnston.portal.user.entity.UserEntity;
import uk.co.dajohnston.portal.user.entity.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

  private final UserRepository userRepository;
  private final HouseholdMemberRepository householdMemberRepository;
  private final GravatarService gravatarService;
  private final ManagementApi auth0ManagementApi;

  public UserProfile getCurrentUser(JwtClaimAccessor jwt) {
    var user = findOrCreateUser(jwt);

    List<Household> households =
        householdMemberRepository.findByUserId(user.getId()).stream()
            .map(
                member ->
                    Household.builder()
                        .id(member.getHousehold().getId())
                        .name(member.getHousehold().getName())
                        .role(member.getRole())
                        .build())
            .toList();

    GetUserResponseContent auth0User = auth0ManagementApi.users().get(user.getAuth0Id());
    return UserProfile.builder()
        .id(user.getId())
        .auth0Id(user.getAuth0Id())
        .email(user.getEmail())
        .givenName(auth0User.getGivenName().orElse(null))
        .familyName(auth0User.getFamilyName().orElse(null))
        .nickname(auth0User.getNickname().orElse(null))
        .picture(getPictureUrl(user, auth0User))
        .households(households)
        .build();
  }

  private String getPictureUrl(UserEntity user, GetUserResponseContent auth0User) {
    if (user.isUseGravatar()) {
      return gravatarService.getGravatarUrl(user.getEmail());
    }
    return auth0User.getPicture().orElse(null);
  }

  public UserProfile updateCurrentUser(JwtClaimAccessor jwt, UpdateUserProfileRequestDto request) {
    var user = findOrCreateUser(jwt);
    if (request.useGravatar() != null) {
      user.setUseGravatar(request.useGravatar());
      userRepository.save(user);
    }

    if (request.nickname() != null
        || request.givenName() != null
        || request.familyName() != null
        || request.picture() != null) {
      auth0ManagementApi
          .users()
          .update(
              user.getAuth0Id(),
              UpdateUserRequestContent.builder()
                  .givenName(Optional.ofNullable(request.givenName()))
                  .familyName(Optional.ofNullable(request.familyName()))
                  .nickname(Optional.ofNullable(request.nickname()))
                  .picture(Optional.ofNullable(request.picture()))
                  .build());
    }

    return getCurrentUser(jwt);
  }

  public UserEntity findOrCreateUser(JwtClaimAccessor jwt) {
    String auth0Id = jwt.getSubject();
    return userRepository.findByAuth0Id(auth0Id).orElseGet(() -> createUser(jwt));
  }

  public Optional<UserEntity> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  private UserEntity createUser(JwtClaimAccessor jwt) {
    return userRepository.save(
        UserEntity.builder()
            .auth0Id(jwt.getSubject())
            .email(jwt.getClaimAsString("email"))
            .useGravatar(false)
            .build());
  }
}
