package com.grahambartley.nightborn.client.smoke;

import com.grahambartley.nightborn.client.particle.ParticleLife;

public final class SmokeLife {

  static final float START_SCALE = 0.6F;
  static final float FULL_SCALE_SHARE = 0.4F;
  static final float FADE_SHARE = 0.35F;
  static final float OPACITY = 0.8F;

  private SmokeLife() {}

  public static float scale(final float age, final int maxAge) {
    final float grown = Math.min(1.0F, ParticleLife.fraction(age, maxAge) / FULL_SCALE_SHARE);
    return START_SCALE + (1.0F - START_SCALE) * grown;
  }

  public static float alpha(final float age, final int maxAge) {
    final float life = ParticleLife.fraction(age, maxAge);
    final float fadeFrom = 1.0F - FADE_SHARE;
    return life <= fadeFrom ? OPACITY : OPACITY * Math.max(0.0F, (1.0F - life) / FADE_SHARE);
  }
}
