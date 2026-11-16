package com.grahambartley.nightborn.blood;

import com.grahambartley.nightborn.config.CoreSettings;
import net.minecraft.world.Difficulty;

public final class BloodMetabolism {

  static final float EXHAUSTION_PER_BLOOD = 4.0F;
  static final int PULSE_TICKS = 200;
  static final int HEAL_ABOVE = BloodAttachment.FULL / 2;
  static final float MAX_EXHAUSTION = 40.0F;

  private BloodMetabolism() {}

  public record State(
      int blood,
      float exhaustion,
      float health,
      float maxHealth,
      Difficulty difficulty,
      boolean naturalRegeneration,
      int flightDrainSeconds,
      int age) {}

  public record Outcome(int blood, float exhaustion, boolean heals) {}

  public static final int GROUNDED = 0;

  public static Outcome tick(final State state, final int restingDrainSeconds) {
    final boolean peaceful = state.difficulty() == Difficulty.PEACEFUL;
    float exhaustion = state.exhaustion();
    if (!peaceful) {
      exhaustion += drainPerTick(restingDrainSeconds);
      if (state.flightDrainSeconds() > GROUNDED) {
        exhaustion += drainPerTick(state.flightDrainSeconds());
      }
    }
    int blood = state.blood();
    if (exhaustion >= EXHAUSTION_PER_BLOOD) {
      exhaustion -= EXHAUSTION_PER_BLOOD;
      if (!peaceful) {
        blood = Math.max(0, blood - 1);
      }
    }
    final boolean heals =
        state.age() % PULSE_TICKS == 0
            && state.naturalRegeneration()
            && blood > HEAL_ABOVE
            && state.health() < state.maxHealth();
    return new Outcome(blood, Math.min(exhaustion, MAX_EXHAUSTION), heals);
  }

  public static int flightDrainSeconds(
      final boolean bat, final boolean levitating, final CoreSettings settings) {
    if (bat) {
      return settings.batDrainSeconds();
    }
    return levitating ? settings.levitationDrainSeconds() : GROUNDED;
  }

  private static float drainPerTick(final int secondsPerBlood) {
    return EXHAUSTION_PER_BLOOD / (secondsPerBlood * 20.0F);
  }
}
