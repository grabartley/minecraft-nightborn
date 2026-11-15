package com.grahambartley.nightborn.decor;

import java.util.Optional;

public enum ChalicePour {
  FILL,
  BOTTLE,
  DRINK;

  public enum Held {
    BLOOD_BOTTLE,
    GLASS_BOTTLE,
    EMPTY_HAND,
    OTHER
  }

  public static Optional<ChalicePour> decide(
      final ChaliceBlood contents, final Held held, final boolean vampire) {
    final boolean full = contents != ChaliceBlood.EMPTY;
    return switch (held) {
      case BLOOD_BOTTLE -> full ? Optional.empty() : Optional.of(FILL);
      case GLASS_BOTTLE -> full ? Optional.of(BOTTLE) : Optional.empty();
      case EMPTY_HAND -> full && vampire ? Optional.of(DRINK) : Optional.empty();
      case OTHER -> Optional.empty();
    };
  }
}
