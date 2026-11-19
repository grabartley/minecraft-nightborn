package com.grahambartley.nightborn.client.occult;

public final class WispLife {

  static final float OPACITY = 0.55F;
  static final float FADE_IN_SHARE = 0.2F;
  static final float START_SCALE = 1.0F;
  static final float END_SCALE = 0.35F;

  private WispLife() {}

  public static float alpha(final float age, final int maxAge) {
    final float life = life(age, maxAge);
    if (life < FADE_IN_SHARE) {
      return OPACITY * life / FADE_IN_SHARE;
    }
    return OPACITY * (1.0F - life) / (1.0F - FADE_IN_SHARE);
  }

  public static float scale(final float age, final int maxAge) {
    return START_SCALE + (END_SCALE - START_SCALE) * life(age, maxAge);
  }

  private static float life(final float age, final int maxAge) {
    return maxAge <= 0 ? 1.0F : Math.min(1.0F, Math.max(0.0F, age) / maxAge);
  }
}
