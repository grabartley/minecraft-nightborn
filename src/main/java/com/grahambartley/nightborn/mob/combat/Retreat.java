package com.grahambartley.nightborn.mob.combat;

import com.grahambartley.nightborn.config.CombatSettings;

public final class Retreat {

  private static final int TICKS_PER_SECOND = 20;

  private Retreat() {}

  public static int healthPercent(final float health, final float maxHealth) {
    return maxHealth <= 0.0F ? 100 : Math.round(100.0F * health / maxHealth);
  }

  public static boolean starts(
      final int healthPercent, final int retreatHealth, final boolean resting) {
    return !resting && retreatHealth > 0 && healthPercent < retreatHealth;
  }

  public static boolean ends(
      final int healthPercent,
      final int retreatHealth,
      final int ticks,
      final CombatSettings settings) {
    return ticks >= settings.retreatLongestSeconds() * TICKS_PER_SECOND
        || healthPercent >= Math.min(100, retreatHealth + settings.retreatRegainPercent());
  }

  public static int restTicks(final CombatSettings settings) {
    return settings.retreatRestSeconds() * TICKS_PER_SECOND;
  }

  public static double speed(final CombatSettings settings) {
    return settings.retreatSpeed() / 100.0;
  }
}
