package com.grahambartley.nightborn.familiar;

public final class HireRules {

  public enum Outcome {
    HIRE,
    NOT_A_VAMPIRE,
    NOT_HIREABLE,
    BUSY,
    ALREADY_KEEPS_ONE
  }

  private HireRules() {}

  public static Outcome decide(
      final boolean vampire,
      final boolean grownVillager,
      final boolean trading,
      final boolean keepsFamiliar) {
    if (!vampire) {
      return Outcome.NOT_A_VAMPIRE;
    }
    if (!grownVillager) {
      return Outcome.NOT_HIREABLE;
    }
    if (trading) {
      return Outcome.BUSY;
    }
    return keepsFamiliar ? Outcome.ALREADY_KEEPS_ONE : Outcome.HIRE;
  }
}
