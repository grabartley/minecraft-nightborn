package com.grahambartley.nightborn.blood;

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
      boolean levitating,
      int age) {}

  public record Outcome(int blood, float exhaustion, boolean heals) {}

  public static Outcome tick(
      final State state, final int restingDrainSeconds, final int levitationDrainSeconds) {
    final boolean peaceful = state.difficulty() == Difficulty.PEACEFUL;
    float exhaustion = state.exhaustion();
    if (!peaceful) {
      exhaustion += drainPerTick(restingDrainSeconds);
      if (state.levitating()) {
        exhaustion += drainPerTick(levitationDrainSeconds);
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

  private static float drainPerTick(final int secondsPerBlood) {
    return EXHAUSTION_PER_BLOOD / (secondsPerBlood * 20.0F);
  }
}
