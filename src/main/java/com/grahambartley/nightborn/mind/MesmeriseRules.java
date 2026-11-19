package com.grahambartley.nightborn.mind;

public final class MesmeriseRules {

  public enum Outcome {
    MESMERISED,
    IMMUNE,
    RESISTED
  }

  private MesmeriseRules() {}

  public static Outcome land(final boolean immune, final double resistance, final float roll) {
    if (immune) {
      return Outcome.IMMUNE;
    }
    return roll < resistance ? Outcome.RESISTED : Outcome.MESMERISED;
  }

  public static boolean immune(
      final boolean boss,
      final boolean self,
      final boolean untouchable,
      final boolean alive,
      final boolean sheltered) {
    return boss || self || untouchable || !alive || sheltered;
  }
}
