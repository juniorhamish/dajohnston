package uk.co.dajohnston.portal.user;

import static java.util.Locale.ROOT;
import static java.util.logging.Level.WARNING;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

@Service
@Log
public class GravatarService {

  private static String md5Hex(String message) {
    try {
      var md = MessageDigest.getInstance("MD5");
      byte[] digest = md.digest(message.getBytes());
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      log.log(WARNING, "MD5 not supported for gravatar URL", e);
      return "";
    }
  }

  public String getGravatarUrl(String email) {
    if (email == null || email.isBlank()) {
      return null;
    }
    String hash = md5Hex(email.toLowerCase(ROOT).trim());
    return "https://www.gravatar.com/avatar/" + hash;
  }
}
