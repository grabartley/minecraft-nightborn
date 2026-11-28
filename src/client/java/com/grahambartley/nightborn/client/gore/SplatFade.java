package com.grahambartley.nightborn.client.gore;

import com.grahambartley.nightborn.client.particle.DecalFade;

public final class SplatFade {

  static final float DARKEST = 0.55F;

  private SplatFade() {}

  public static float shade(final int age) {
    return DARKEST + (1.0F - DARKEST) * DecalFade.wetness(age);
  }
}
