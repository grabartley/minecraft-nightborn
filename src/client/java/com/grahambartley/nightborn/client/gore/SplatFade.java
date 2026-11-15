package com.grahambartley.nightborn.client.gore;

public final class SplatFade {

  static final float FRESH_SHARE = 0.35F;
  static final float FADE_SHARE = 0.2F;
  static final float DARKEST = 0.55F;

  private SplatFade() {}

  public static float shade(final int age, final int maxAge) {
    final float life = life(age, maxAge);
    if (life <= FRESH_SHARE) {
      return 1.0F;
    }
    final float drying = Math.min(1.0F, (life - FRESH_SHARE) / (1.0F - FADE_SHARE - FRESH_SHARE));
    return 1.0F - drying * (1.0F - DARKEST);
  }

  public static float alpha(final int age, final int maxAge) {
    final float life = life(age, maxAge);
    final float fadeFrom = 1.0F - FADE_SHARE;
    return life <= fadeFrom ? 1.0F : Math.max(0.0F, (1.0F - life) / FADE_SHARE);
  }

  private static float life(final int age, final int maxAge) {
    return maxAge <= 0 ? 1.0F : Math.min(1.0F, Math.max(0, age) / (float) maxAge);
  }
}
