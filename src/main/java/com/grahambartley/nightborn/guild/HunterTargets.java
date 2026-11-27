package com.grahambartley.nightborn.guild;

public final class HunterTargets {

  private HunterTargets() {}

  public static boolean hunts(final boolean vampire, final boolean raider, final boolean exempt) {
    return (vampire || raider) && !exempt;
  }

  public static boolean defends(
      final boolean attackedUs, final boolean player, final boolean guild) {
    return attackedUs && !player && !guild;
  }
}
