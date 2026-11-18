package com.grahambartley.nightborn.raid;

public final class HouseGuards {

  private HouseGuards() {}

  public static boolean intrudes(
      final boolean player,
      final boolean vampire,
      final boolean creativeOrSpectator,
      final boolean hunterMob) {
    if (vampire || creativeOrSpectator) {
      return false;
    }
    return player || hunterMob;
  }

  public static boolean guardsAgainst(
      final boolean monster, final boolean vampire, final boolean keepsHouse) {
    return monster && !(keepsHouse && vampire);
  }
}
