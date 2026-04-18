package uk.co.dajohnston.portal.config;

public class ConfigurationException extends RuntimeException {

  public ConfigurationException(Throwable cause) {
    super("Unable to configure the application.", cause);
  }
}
