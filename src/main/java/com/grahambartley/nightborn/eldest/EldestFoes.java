package com.grahambartley.nightborn.eldest;

public final class EldestFoes {

  private EldestFoes() {}

  public static boolean fights(final boolean vampire, final boolean player, final boolean struck) {
    return !vampire || player && struck;
  }

  public static boolean shrugsOff(
      final boolean changing, final boolean fromOutside, final boolean finishing) {
    return !finishing && (changing || fromOutside);
  }

  public static boolean seeksOut(final boolean vampire, final boolean peaceful) {
    return !vampire && !peaceful;
  }
}
