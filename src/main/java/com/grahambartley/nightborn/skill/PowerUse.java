package com.grahambartley.nightborn.skill;

import java.util.Locale;

public final class PowerUse {

  public enum Verdict {
    READY,
    NO_POWER,
    HELPLESS,
    WEAKENED,
    COOLING,
    THIRSTY;

    public String translationKey() {
      return "nightborn.powers.verdict." + name().toLowerCase(Locale.ROOT);
    }
  }

  private PowerUse() {}

  public static Verdict check(
      final boolean hasPower,
      final boolean helpless,
      final boolean weakened,
      final long cooldownTicksLeft,
      final int blood,
      final int bloodCost) {
    if (!hasPower) {
      return Verdict.NO_POWER;
    }
    if (helpless) {
      return Verdict.HELPLESS;
    }
    if (weakened) {
      return Verdict.WEAKENED;
    }
    if (cooldownTicksLeft > 0) {
      return Verdict.COOLING;
    }
    if (blood < bloodCost) {
      return Verdict.THIRSTY;
    }
    return Verdict.READY;
  }
}
