package uk.co.dajohnston.portal.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.portal.dto.UserProfileResponse;
import uk.co.dajohnston.portal.model.User;
import uk.co.dajohnston.portal.repository.HouseholdMemberRepository;
import uk.co.dajohnston.portal.repository.UserRepository;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserRepository userRepository;
  private final HouseholdMemberRepository householdMemberRepository;

  @GetMapping("/me")
  public UserProfileResponse getCurrentUser(@AuthenticationPrincipal JwtClaimAccessor jwt) {
    String auth0Id = jwt.getSubject();

    // Auto-create user if not found
    User user =
        userRepository
            .findByAuth0Id(auth0Id)
            .orElseGet(
                () ->
                    userRepository.save(
                        User.builder()
                            .auth0Id(auth0Id)
                            .email(jwt.getClaimAsString("email"))
                            .displayName(jwt.getClaimAsString("name"))
                            .build()));

    List<UserProfileResponse.HouseholdDto> households =
        householdMemberRepository.findByUserId(user.getId()).stream()
            .map(
                member ->
                    UserProfileResponse.HouseholdDto.builder()
                        .id(member.getHousehold().getId())
                        .name(member.getHousehold().getName())
                        .role(member.getRole())
                        .build())
            .toList();

    return UserProfileResponse.builder()
        .id(user.getId())
        .auth0Id(user.getAuth0Id())
        .email(user.getEmail())
        .displayName(user.getDisplayName())
        .households(households)
        .build();
  }
}
