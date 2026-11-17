package com.grahambartley.nightborn.name;

import java.util.Locale;
import java.util.Optional;

public final class Epithets {

  private static final String PREFIX = "epithet.nightborn.";
  private static final String ARTICLE = "the ";

  private Epithets() {}

  public static String slug(final String epithet) {
    final String lower = epithet.toLowerCase(Locale.ROOT);
    final String bare = lower.startsWith(ARTICLE) ? lower.substring(ARTICLE.length()) : lower;
    return bare.replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
  }

  public static String key(final String epithet, final boolean female) {
    return PREFIX + slug(epithet) + (female ? ".female" : ".male");
  }

  public static String join(final String given, final String epithet) {
    return epithet.isEmpty() ? given : given + " " + epithet;
  }

  public static Optional<String> given(final String fullName, final String epithet) {
    final String suffix = " " + epithet;
    return !epithet.isEmpty() && fullName.endsWith(suffix) && fullName.length() > suffix.length()
        ? Optional.of(fullName.substring(0, fullName.length() - suffix.length()))
        : Optional.empty();
  }
}
