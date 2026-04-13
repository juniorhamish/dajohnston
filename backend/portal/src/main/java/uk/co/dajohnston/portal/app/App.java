package uk.co.dajohnston.portal.app;

import lombok.Builder;

@Builder
public record App(String id, String name, String description, String icon, String url) {}
