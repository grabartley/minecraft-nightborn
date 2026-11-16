package com.grahambartley.nightborn.client.structure;

import java.util.Optional;
import net.minecraft.util.Identifier;
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

  public static boolean finished(final float volume, final float target) {
    return volume <= 0.0F && target <= 0.0F;
  }

  public static LoopAction loopAction(
      final Optional<Identifier> playing,
      final boolean done,
      final Optional<Identifier> wanted,
      final float closeness) {
    final boolean audible = wanted.isPresent() && closeness > 0.0F;
    if (playing.isEmpty() || done) {
      return audible ? LoopAction.START : LoopAction.NOTHING;
    }
    if (wanted.equals(playing)) {
      return LoopAction.KEEP;
    }
    return audible ? LoopAction.REPLACE : LoopAction.STOP;
  }
}
