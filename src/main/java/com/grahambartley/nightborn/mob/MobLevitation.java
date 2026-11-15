package com.grahambartley.nightborn.mob;

public final class MobLevitation {

  static final float START_CHANCE = 1.0F / 600.0F;
  static final float STOP_CHANCE = 1.0F / 300.0F;

  private MobLevitation() {}

  public static boolean wants(final boolean levitating, final boolean fighting, final float roll) {
    if (fighting) {
      return true;
    }
    return levitating ? roll >= STOP_CHANCE : roll < START_CHANCE;
  }
}
