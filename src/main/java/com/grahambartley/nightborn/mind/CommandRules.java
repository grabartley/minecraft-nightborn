package com.grahambartley.nightborn.mind;

public final class CommandRules {

  static final double FOLLOW_GAP = 3.0;
  static final double DOOR_REACH = 2.0;
  static final double STRIKE_REACH = 2.0;
  static final int STRIKE_EVERY_TICKS = 20;
  static final float BARE_HAND_DAMAGE = 1.0F;

  private CommandRules() {}

  public static boolean commandable(
      final boolean player, final boolean mesmerisedByCaster, final boolean boss) {
    return !player && mesmerisedByCaster && !boss;
  }

  public static boolean closesIn(final double distance, final double gap) {
    return distance > gap;
  }

  public static boolean strikes(final double distance, final long now, final long lastStrike) {
    return distance <= STRIKE_REACH && now - lastStrike >= STRIKE_EVERY_TICKS;
  }
}
