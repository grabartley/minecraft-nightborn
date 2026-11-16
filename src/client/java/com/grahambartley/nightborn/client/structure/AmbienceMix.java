package com.grahambartley.nightborn.client.structure;

import net.minecraft.util.math.MathHelper;

public final class AmbienceMix {

  public static final float FADE_STEP = 1.0F / 40.0F;
  public static final float QUIETEST_ADDITION = 0.35F;

  private AmbienceMix() {}

  public static float approach(final float volume, final float target) {
    return volume + MathHelper.clamp(target - volume, -FADE_STEP, FADE_STEP);
  }

  public static boolean addsOneShot(
      final double tickChance, final float closeness, final double roll) {
    return closeness > 0.0F && roll < tickChance;
  }

  public static float additionVolume(final float closeness) {
    return MathHelper.lerp(MathHelper.clamp(closeness, 0.0F, 1.0F), QUIETEST_ADDITION, 1.0F);
  }
}
