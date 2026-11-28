package com.grahambartley.nightborn.client.particle;

public final class DecalFade {

  static final float DISCARD_BELOW = 0.1F;
  static final float FAINTEST_PIXEL = 0.25F;
  static final float CUTOFF = DISCARD_BELOW * 1.02F;
  static final float PAST_SOLID = 1.06F;

  private DecalFade() {}

  public static float wetness(final int age) {
    final int drying = age - DecalTiming.FRESH_TICKS;
    if (drying <= 0) {
      return 1.0F;
    }
    return Math.max(0.0F, 1.0F - drying / (float) DecalTiming.DRYING_TICKS);
  }

  public static float alpha(final int age, final int maxAge, final int fadeTicks) {
    final int fadeFrom = maxAge - Math.max(1, fadeTicks);
    if (age <= fadeFrom) {
      return 1.0F;
    }
    final float progress = Math.min(1.0F, (age - fadeFrom) / (float) Math.max(1, fadeTicks));
    final float faintestKept = FAINTEST_PIXEL + (PAST_SOLID - FAINTEST_PIXEL) * progress;
    return Math.min(1.0F, CUTOFF / faintestKept);
  }
}
