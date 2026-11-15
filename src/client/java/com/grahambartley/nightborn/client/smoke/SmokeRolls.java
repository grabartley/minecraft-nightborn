package com.grahambartley.nightborn.client.smoke;

public final class SmokeRolls {

  static final int MIN_AGE = 30;
  static final int AGE_SPREAD = 20;
  static final float MIN_SIZE = 0.4F;
  static final float SIZE_SPREAD = 0.25F;
  static final float DARKEST_SHADE = 0.8F;

  private SmokeRolls() {}

  public static int maxAge(final float roll) {
    return MIN_AGE + Math.min(AGE_SPREAD - 1, (int) (clamp(roll) * AGE_SPREAD));
  }

  public static float fullSize(final float roll) {
    return MIN_SIZE + clamp(roll) * SIZE_SPREAD;
  }

  public static float shade(final float roll) {
    return DARKEST_SHADE + clamp(roll) * (1.0F - DARKEST_SHADE);
  }

  private static float clamp(final float roll) {
    return Math.min(1.0F, Math.max(0.0F, roll));
  }
}
