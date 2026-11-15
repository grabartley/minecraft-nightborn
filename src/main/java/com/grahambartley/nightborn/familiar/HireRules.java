package com.grahambartley.nightborn.familiar;

public final class HireRules {

  public enum Outcome {
    HIRE,
    NOT_A_VAMPIRE,
    NOT_HIREABLE,
    ALREADY_KEEPS_ONE
  }

  private HireRules() {}

  public static Outcome decide(
      final boolean vampire, final boolean adultVillager, final boolean keepsFamiliar) {
    if (!vampire) {
      return Outcome.NOT_A_VAMPIRE;
    }
    if (!adultVillager) {
      return Outcome.NOT_HIREABLE;
    }
    return keepsFamiliar ? Outcome.ALREADY_KEEPS_ONE : Outcome.HIRE;
  }
}
