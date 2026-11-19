package com.grahambartley.nightborn.oldblood;

import java.util.List;
import java.util.Optional;

public final class FontClips {

  public static final String DRINK = "drink";
  public static final String REFILL = "refill";

  private FontLevel seen;

  public Optional<List<String>> observe(final FontLevel level) {
    final FontLevel previous = this.seen;
    this.seen = level;
    if (previous == level) {
      return Optional.empty();
    }
    if (previous != null && previous.full()) {
      return Optional.of(List.of(DRINK, level.clip()));
    }
    if (previous != null && level.full()) {
      return Optional.of(List.of(REFILL, level.clip()));
    }
    return Optional.of(List.of(level.clip()));
  }
}
