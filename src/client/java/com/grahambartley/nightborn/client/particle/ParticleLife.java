package com.grahambartley.nightborn.client.particle;

public final class ParticleLife {

  private ParticleLife() {}

  public static float fraction(final float age, final int maxAge) {
    return maxAge <= 0 ? 1.0F : Math.min(1.0F, Math.max(0.0F, age) / maxAge);
  }
}
