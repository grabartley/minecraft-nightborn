package com.grahambartley.nightborn.mob.brain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Aim {
  SELF,
  TARGET,
  ALLY,
  ALLIES,
  FOES,
  SPOT;

  private static final Map<String, Aim> BY_KEY =
      Arrays.stream(values()).collect(Collectors.toMap(Aim::key, Function.identity()));

  public String key() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static Optional<Aim> byKey(final String key) {
    return Optional.ofNullable(BY_KEY.get(key));
  }
}
