package com.grahambartley.nightborn.client.particle;

public final class ParticleLife {

  private ParticleLife() {}

  public static float lerp(final float start, final float end, final float age, final int maxAge) {
    return start + (end - start) * fraction(age, maxAge);
  }

  public static float fraction(final float age, final int maxAge) {
    return maxAge <= 0 ? 1.0F : Math.min(1.0F, Math.max(0.0F, age) / maxAge);
  }
}
