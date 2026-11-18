package com.grahambartley.nightborn.feeding;

public final class BiteResistance {

  private BiteResistance() {}

  public static int sicknessChance(final int chance, final double resistance) {
    return (int) Math.round(chance * (1.0 - clamp(resistance)));
  }

  public static float breakFreeChance(final float chance, final double resistance) {
    return (float) Math.min(1.0, chance * (1.0 + clamp(resistance)));
  }

  public static int weaknessSeconds(final int seconds, final double resistance) {
    return (int) Math.round(seconds * (1.0 - clamp(resistance)));
  }

  static double clamp(final double resistance) {
    return Math.max(0.0, Math.min(1.0, resistance));
  }
}
