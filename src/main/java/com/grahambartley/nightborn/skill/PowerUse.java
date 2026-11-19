package com.grahambartley.nightborn.skill;

import java.util.Locale;

public final class PowerUse {

  public enum Verdict {
    READY,
    NO_POWER,
    HELPLESS,
    HELD,
    BARRED,
    GROUNDED,
    IN_WINGED_FORM,
    AS_A_BAT,
    WEAKENED,
    SUNLIT,
    COOLING,
    THIRSTY,
    CRAMPED,
    UNSEEN,
    NOWHERE,
    NO_GROUND,
    NO_SUBJECT,
    REBUKED,
    NO_CRUCIFIX,
    NO_WEAPON,
    TOO_HURT,
    SMOTHERED;

    public String translationKey() {
      return "nightborn.powers.verdict." + name().toLowerCase(Locale.ROOT);
    }
  }

  public record Situation(
      boolean hasPower,
      boolean helpless,
      boolean held,
      boolean barred,
      boolean grounded,
      boolean winged,
      boolean bat,
      boolean weakened,
      boolean sunlit,
      long cooldownTicksLeft,
      int blood,
      int bloodCost,
      boolean unseen,
      boolean nowhere,
      boolean groundless,
      boolean subjectless,
      boolean rebuked,
      boolean crossless,
      boolean unarmed,
      boolean hurt,
      boolean smothered) {}

  private PowerUse() {}

  public static Verdict check(final Situation situation) {
    if (!situation.hasPower()) {
      return Verdict.NO_POWER;
    }
    if (situation.helpless()) {
      return Verdict.HELPLESS;
    }
    if (situation.held()) {
      return Verdict.HELD;
    }
    if (situation.rebuked()) {
      return Verdict.REBUKED;
    }
    if (situation.barred()) {
      return Verdict.BARRED;
    }
    if (situation.grounded()) {
      return Verdict.GROUNDED;
    }
    if (situation.winged()) {
      return Verdict.IN_WINGED_FORM;
    }
    if (situation.bat()) {
      return Verdict.AS_A_BAT;
    }
    if (situation.weakened()) {
      return Verdict.WEAKENED;
    }
    if (situation.sunlit()) {
      return Verdict.SUNLIT;
    }
    if (situation.cooldownTicksLeft() > 0) {
      return Verdict.COOLING;
    }
    if (situation.blood() < situation.bloodCost()) {
      return Verdict.THIRSTY;
    }
    if (situation.unseen()) {
      return Verdict.UNSEEN;
    }
    if (situation.nowhere()) {
      return Verdict.NOWHERE;
    }
    if (situation.groundless()) {
      return Verdict.NO_GROUND;
    }
    if (situation.subjectless()) {
      return Verdict.NO_SUBJECT;
    }
    if (situation.crossless()) {
      return Verdict.NO_CRUCIFIX;
    }
    if (situation.unarmed()) {
      return Verdict.NO_WEAPON;
    }
    if (situation.hurt()) {
      return Verdict.TOO_HURT;
    }
    if (situation.smothered()) {
      return Verdict.SMOTHERED;
    }
    return Verdict.READY;
  }
}
