package uk.co.dajohnston.portal.user;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    return UserProfile.builder()
        .id(user.getId())
        .auth0Id(user.getAuth0Id())
        .email(user.getEmail())
        .displayName(user.getDisplayName())
        .households(households)
        .build();
  }

  public UserProfile updateCurrentUser(JwtClaimAccessor jwt, String displayName) {
    var user = findOrCreateUser(jwt);
    user.setDisplayName(displayName);
    userRepository.save(user);
    return getCurrentUser(jwt);
  }

  public UserEntity findOrCreateUser(JwtClaimAccessor jwt) {
    String auth0Id = jwt.getSubject();
    return userRepository.findByAuth0Id(auth0Id).orElseGet(() -> createUser(jwt));
  }

  private UserEntity createUser(JwtClaimAccessor jwt) {
    return userRepository.save(
        UserEntity.builder()
            .auth0Id(jwt.getSubject())
            .email(jwt.getClaimAsString("email"))
            .displayName(jwt.getClaimAsString("name"))
            .build());
  }
}
