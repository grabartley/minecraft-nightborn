package com.grahambartley.nightborn.balance;

import java.util.Optional;

public final class RoundRule {

  public static final int TIME_LIMIT_TICKS = 20 * 180;

  public enum Verdict {
    ONGOING,
    VAMPIRES,
    HUNTERS,
    DRAW;

    public boolean over() {
      return this != ONGOING;
    }

    public Optional<Side> winner() {
      return switch (this) {
        case VAMPIRES -> Optional.of(Side.VAMPIRES);
        case HUNTERS -> Optional.of(Side.HUNTERS);
        default -> Optional.empty();
      };
    }
  }

  private RoundRule() {}

  public static Verdict judge(
      final int vampiresStanding, final int huntersStanding, final int ticks) {
    if (vampiresStanding == 0 && huntersStanding == 0) {
      return Verdict.DRAW;
    }
    if (vampiresStanding == 0) {
      return Verdict.HUNTERS;
    }
    if (huntersStanding == 0) {
      return Verdict.VAMPIRES;
    }
    return ticks >= TIME_LIMIT_TICKS ? Verdict.DRAW : Verdict.ONGOING;
  }
}
