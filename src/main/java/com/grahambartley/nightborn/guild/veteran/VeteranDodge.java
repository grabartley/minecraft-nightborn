package com.grahambartley.nightborn.guild.veteran;

public final class VeteranDodge {

  public static final double TELEPORT = 3.0;

  private VeteranDodge() {}

  public static boolean dodged(final double movedInOneTick) {
    return movedInOneTick > TELEPORT;
  }

  public static boolean chainLands(
      final boolean dodged,
      final boolean alive,
      final boolean vanished,
      final boolean sees,
      final double distance,
      final double reach) {
    return !dodged && alive && !vanished && sees && distance <= reach;
  }
}
