package com.grahambartley.nightborn.mob.combat;

public final class Retreat {

  public static final int REGAIN_PERCENT = 30;
  public static final int LONGEST_TICKS = 160;
  public static final int REST_TICKS = 300;

  private Retreat() {}

  public static int healthPercent(final float health, final float maxHealth) {
    return maxHealth <= 0.0F ? 100 : Math.round(100.0F * health / maxHealth);
  }

  public static boolean starts(
      final int healthPercent, final int retreatHealth, final boolean resting) {
    return !resting && retreatHealth > 0 && healthPercent < retreatHealth;
  }

  public static boolean ends(final int healthPercent, final int retreatHealth, final int ticks) {
    return ticks >= LONGEST_TICKS || healthPercent >= Math.min(100, retreatHealth + REGAIN_PERCENT);
  }
}
