package com.grahambartley.nightborn.guild;

public final class HunterTargets {

  private HunterTargets() {}

  public static boolean hunts(final boolean vampire, final boolean raider, final boolean exempt) {
    return (vampire || raider) && !exempt;
  }

  public static boolean noticesNow(
      final double distanceSquared, final double near, final boolean chanceComesUp) {
    return chanceComesUp || distanceSquared <= near * near;
  }
}
