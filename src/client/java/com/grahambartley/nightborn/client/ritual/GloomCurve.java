package com.grahambartley.nightborn.client.ritual;

import net.minecraft.util.math.MathHelper;

public final class GloomCurve {

  public static final int LINGER_TICKS = 200;

  private GloomCurve() {}

  public static float strength(final long elapsed, final int buildUpTicks) {
    if (elapsed < 0 || elapsed >= (long) buildUpTicks + LINGER_TICKS) {
      return 0.0F;
    }
    if (elapsed < buildUpTicks) {
      return buildUpTicks <= 0 ? 1.0F : (float) elapsed / buildUpTicks;
    }
    return 1.0F - (float) (elapsed - buildUpTicks) / LINGER_TICKS;
  }

  public static float reach(final double distance, final int range) {
    if (range <= 0) {
      return 0.0F;
    }
    return (float) MathHelper.clamp(2.0 * (1.0 - distance / range), 0.0, 1.0);
  }
}
