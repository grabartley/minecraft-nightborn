package com.grahambartley.nightborn.eldest;

public final class EldestFoes {

  private EldestFoes() {}

  public static boolean fights(
      final boolean vampire, final boolean player, final boolean struck, final boolean summoner) {
    return !vampire || player && (struck || summoner);
  }

  public static boolean shrugsOff(
      final boolean changing,
      final boolean fromOutside,
      final boolean surroundings,
      final boolean finishing) {
    return !finishing && (changing || fromOutside || surroundings);
  }

  public static boolean mounts(final boolean seat) {
    return seat;
  }

  public static boolean seeksOut(
      final boolean vampire, final boolean peaceful, final boolean summoner) {
    return (!vampire || summoner) && !peaceful;
  }
}
