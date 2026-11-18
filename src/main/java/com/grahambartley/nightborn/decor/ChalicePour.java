package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.blood.BloodQuality;
import java.util.Optional;

public enum ChalicePour {
  FILL(null),
  BOTTLE(null),
  DRINK(null),
  WRONG_QUALITY("nightborn.chalice.wrong_quality"),
  BRIMMING("nightborn.chalice.full");

  public enum Held {
    BLOOD_BOTTLE,
    GLASS_BOTTLE,
    EMPTY_HAND,
    OTHER
  }

  private final String refusal;

  ChalicePour(final String refusal) {
    this.refusal = refusal;
  }

  public static Optional<ChalicePour> decide(
      final ChaliceContents contents,
      final Held held,
      final Optional<BloodQuality> poured,
      final boolean vampire) {
    return switch (held) {
      case BLOOD_BOTTLE -> Optional.of(pour(contents, poured.orElseThrow()));
      case GLASS_BOTTLE -> contents.empty() ? Optional.empty() : Optional.of(BOTTLE);
      case EMPTY_HAND -> !contents.empty() && vampire ? Optional.of(DRINK) : Optional.empty();
      case OTHER -> Optional.empty();
    };
  }

  private static ChalicePour pour(final ChaliceContents contents, final BloodQuality poured) {
    if (contents.full()) {
      return BRIMMING;
    }
    return contents.takes(poured) ? FILL : WRONG_QUALITY;
  }

  public Optional<String> refusal() {
    return Optional.ofNullable(refusal);
  }
}
