package com.grahambartley.nightborn.familiar;

import java.util.Locale;

public enum FamiliarMood {
  DEVOTED(75),
  CONTENT(50),
  UNEASY(25),
  RESENTFUL(0);

  private final int atLeast;

  FamiliarMood(final int atLeast) {
    this.atLeast = atLeast;
  }

  public static FamiliarMood of(final int loyalty, final boolean turned) {
    if (turned) {
      return CONTENT;
    }
    for (final FamiliarMood mood : values()) {
      if (loyalty >= mood.atLeast) {
        return mood;
      }
    }
    return RESENTFUL;
  }

  public String translationKey() {
    return "nightborn.familiar.mood." + name().toLowerCase(Locale.ROOT);
  }
}
