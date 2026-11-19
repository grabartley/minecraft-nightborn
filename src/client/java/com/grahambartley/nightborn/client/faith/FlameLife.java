package com.grahambartley.nightborn.client.faith;

import com.grahambartley.nightborn.client.particle.ParticleLife;

public final class FlameLife {

  static final float OPACITY = 0.95F;
  static final float FADE_SHARE = 0.35F;
  static final float START_SCALE = 1.0F;
  static final float END_SCALE = 0.45F;

  private FlameLife() {}

  public static float alpha(final float age, final int maxAge) {
    final float life = ParticleLife.fraction(age, maxAge);
    final float fadeFrom = 1.0F - FADE_SHARE;
    if (life <= fadeFrom) {
      return OPACITY;
    }
    return OPACITY * (1.0F - life) / FADE_SHARE;
  }

  public static float scale(final float age, final int maxAge) {
    return ParticleLife.lerp(START_SCALE, END_SCALE, age, maxAge);
  }
}
