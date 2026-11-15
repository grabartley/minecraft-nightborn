package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.config.BloodSettings;
import com.grahambartley.nightborn.weakened.WeakenedSources;

public final class Starvation {

  public static final String SOURCE = "starving";

  static final float HEALTH_FLOOR = 1.0F;
  static final float HURT_PER_PULSE = 1.0F;
  static final int TICKS_PER_SECOND = 20;

  private Starvation() {}

  public record Outcome(int ticks, int weakness, float hurt) {}

  public static Outcome tick(
      final int ticks,
      final int blood,
      final float health,
      final boolean peaceful,
      final BloodSettings settings) {
    final int next = blood <= 0 ? ticks + 1 : blood >= BloodAttachment.FULL ? 0 : ticks;
    final int weakness =
        strength(next, settings)
            * (BloodAttachment.FULL - Math.max(0, blood))
            / BloodAttachment.FULL;
    final float hurt =
        blood <= 0 && !peaceful && hurtsOn(next, settings) ? hurtAmount(health) : 0.0F;
    return new Outcome(next, weakness, hurt);
  }

  public static int strength(final int ticks, final BloodSettings settings) {
    if (ticks <= 0) {
      return 0;
    }
    final long steps = 1L + ticks / ((long) settings.starvingStepSeconds() * TICKS_PER_SECOND);
    return (int) Math.min(WeakenedSources.CAP, steps * settings.starvingStep());
  }

  public static double nightGiftShare(final int weakness) {
    return Math.max(0.0, 1.0 - weakness / (WeakenedSources.CAP / 2.0));
  }

  public static double bareHandShare(final int weakness) {
    return 1.0 - 0.5 * Math.min(WeakenedSources.CAP, Math.max(0, weakness)) / WeakenedSources.CAP;
  }

  static boolean hurtsOn(final int ticks, final BloodSettings settings) {
    final int sinceDelay = ticks - settings.starvingHurtDelaySeconds() * TICKS_PER_SECOND;
    return sinceDelay > 0 && sinceDelay % (settings.starvingHurtSeconds() * TICKS_PER_SECOND) == 0;
  }

  static float hurtAmount(final float health) {
    return Math.max(0.0F, Math.min(HURT_PER_PULSE, health - HEALTH_FLOOR));
  }
}
