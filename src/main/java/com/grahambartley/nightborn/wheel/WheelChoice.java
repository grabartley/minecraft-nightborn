package com.grahambartley.nightborn.wheel;

public final class WheelChoice {

  private WheelChoice() {}

  public static boolean accepted(
      final boolean shown, final boolean targetThere, final boolean alive, final boolean inReach) {
    return shown && targetThere && alive && inReach;
  }
}
