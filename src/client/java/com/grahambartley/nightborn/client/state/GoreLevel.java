package com.grahambartley.nightborn.client.state;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum GoreLevel {
  FULL,
  REDUCED,
  OFF;

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public String translationKey() {
    return "nightborn.client.gore." + id();
  }

  public static Optional<GoreLevel> byId(final String id) {
    return Arrays.stream(values()).filter(level -> level.id().equals(id)).findFirst();
  }
}
