package com.grahambartley.nightborn.mob.brain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Intent {
  FIGHT,
  RETREAT,
  CLOSE_IN,
  HUNT,
  TRAVEL,
  GUARD,
  REST;

  private static final Map<String, Intent> BY_KEY =
      Arrays.stream(values()).collect(Collectors.toMap(Intent::key, Function.identity()));

  public String key() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<Intent> byKey(final String key) {
    return Optional.ofNullable(BY_KEY.get(key));
  }
}
