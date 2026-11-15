package com.grahambartley.nightborn.client.smoke;

import net.minecraft.util.math.random.Random;

public final class SmokeLife {

  static final float START_SCALE = 0.6F;
  static final float FULL_SCALE_SHARE = 0.4F;
  static final float FADE_SHARE = 0.35F;
  static final float OPACITY = 0.8F;
  static final int MIN_AGE = 30;
  static final int AGE_SPREAD = 20;
  static final float MIN_SIZE = 0.4F;
  static final float SIZE_SPREAD = 0.25F;
  static final float DARKEST_SHADE = 0.8F;

  private SmokeLife() {}

  public static int maxAge(final Random random) {
    return MIN_AGE + random.nextInt(AGE_SPREAD);
  }

  public static float fullSize(final Random random) {
    return MIN_SIZE + random.nextFloat() * SIZE_SPREAD;
  }

  public static float shade(final Random random) {
    return DARKEST_SHADE + random.nextFloat() * (1.0F - DARKEST_SHADE);
  }

  public static float scale(final float age, final int maxAge) {
    final float grown = Math.min(1.0F, life(age, maxAge) / FULL_SCALE_SHARE);
    return START_SCALE + (1.0F - START_SCALE) * grown;
  }

  public static float alpha(final float age, final int maxAge) {
    final float life = life(age, maxAge);
    final float fadeFrom = 1.0F - FADE_SHARE;
    return life <= fadeFrom ? OPACITY : OPACITY * Math.max(0.0F, (1.0F - life) / FADE_SHARE);
  }

  private static float life(final float age, final int maxAge) {
    return maxAge <= 0 ? 1.0F : Math.min(1.0F, Math.max(0.0F, age) / maxAge);
  }
}
