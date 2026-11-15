package com.grahambartley.nightborn.client.fog;

import net.minecraft.util.math.MathHelper;

public final class FogDepth {

  public static final float HEART_END = 20.0F;
  public static final float HEART_START = 0.0F;
  public static final float STEP = 0.05F;

  private FogDepth() {}

  public static float end(final float vanillaEnd, final float closeness) {
    if (vanillaEnd <= HEART_END) {
      return vanillaEnd;
    }
    return MathHelper.lerp(eased(closeness), vanillaEnd, HEART_END);
  }

  public static float start(final float vanillaStart, final float closeness, final float end) {
    return Math.min(MathHelper.lerp(eased(closeness), vanillaStart, HEART_START), end);
  }

  public static float approach(final float shown, final float target) {
    return shown + MathHelper.clamp(target - shown, -STEP, STEP);
  }

  private static float eased(final float closeness) {
    final float c = MathHelper.clamp(closeness, 0.0F, 1.0F);
    return c * (2.0F - c);
  }
}
