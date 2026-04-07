package uk.co.dajohnston.portal.user.web;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtClaimAccessor;
import org.springframework.web.bind.annotation.RestController;
import uk.co.dajohnston.api.UsersApi;
import uk.co.dajohnston.model.UpdateUserProfileRequestDto;
import uk.co.dajohnston.model.UserProfileDto;
import uk.co.dajohnston.portal.user.UserMapper;
import uk.co.dajohnston.portal.user.UserService;

@RestController
@RequiredArgsConstructor
class UserController implements UsersApi {

  private final UserService userService;
  private final UserMapper userMapper;

  @Override
  public ResponseEntity<UserProfileDto> getCurrentUser(
      @AuthenticationPrincipal JwtClaimAccessor jwt) {
    return ResponseEntity.ok(userMapper.toDto(userService.getCurrentUser(jwt)));
  }

  @Override
  public ResponseEntity<UserProfileDto> updateCurrentUser(
      @AuthenticationPrincipal JwtClaimAccessor jwt, UpdateUserProfileRequestDto body) {
    return ResponseEntity.ok(
        userMapper.toDto(userService.updateCurrentUser(jwt, body.displayName())));
  }
}
