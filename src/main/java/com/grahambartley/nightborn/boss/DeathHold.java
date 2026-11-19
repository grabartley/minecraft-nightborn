package com.grahambartley.nightborn.boss;

public final class DeathHold {

  static final int LAST_DEATH_TICK = 19;

  private int dyingTicks;

  public boolean lingers(final int hold) {
    if (dyingTicks < hold) {
      dyingTicks++;
      return true;
    }
    return false;
  }

  public static int deathTime(final int deathTime, final int hold) {
    return hold > 0 ? Math.max(deathTime, LAST_DEATH_TICK) : deathTime;
  }
}
