package com.grahambartley.nightborn.blood;

import net.minecraft.world.Difficulty;

public final class BloodMetabolism {

  static final float EXHAUSTION_PER_BLOOD = 4.0F;
  static final float HEAL_EXHAUSTION = 6.0F;
  static final int PULSE_TICKS = 80;
  static final int HEAL_THRESHOLD = 18;
  static final float EASY_STARVE_FLOOR = 10.0F;
  static final float NORMAL_STARVE_FLOOR = 1.0F;

  private BloodMetabolism() {}

  public record State(
      int blood,
      float exhaustion,
      float health,
      float maxHealth,
      Difficulty difficulty,
      boolean naturalRegeneration,
      int age) {}

  public record Outcome(int blood, float exhaustion, boolean heals, boolean starves) {}

  public static Outcome tick(final State state, final int restingDrainSeconds) {
    final boolean peaceful = state.difficulty() == Difficulty.PEACEFUL;
    float exhaustion =
        state.exhaustion()
            + (peaceful ? 0.0F : EXHAUSTION_PER_BLOOD / (restingDrainSeconds * 20.0F));
    int blood = state.blood();
    if (exhaustion >= EXHAUSTION_PER_BLOOD) {
      exhaustion -= EXHAUSTION_PER_BLOOD;
      if (!peaceful) {
        blood = Math.max(0, blood - 1);
      }
    }
    final boolean pulse = state.age() % PULSE_TICKS == 0;
    final boolean heals =
        pulse
            && state.naturalRegeneration()
            && blood >= HEAL_THRESHOLD
            && state.health() < state.maxHealth();
    if (heals) {
      exhaustion += HEAL_EXHAUSTION;
    }
    final boolean starves = pulse && blood <= 0 && canStarve(state.health(), state.difficulty());
    return new Outcome(blood, Math.min(exhaustion, 40.0F), heals, starves);
  }

  static boolean canStarve(final float health, final Difficulty difficulty) {
    return health > EASY_STARVE_FLOOR
        || difficulty == Difficulty.HARD
        || (difficulty == Difficulty.NORMAL && health > NORMAL_STARVE_FLOOR);
  }
}
