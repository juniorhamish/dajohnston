package uk.co.dajohnston.portal.notification;

public record SubscriptionDetails(
    String endpoint, Long expirationTime, String p256Dh, String auth) {}
