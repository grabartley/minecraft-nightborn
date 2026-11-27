package com.grahambartley.nightborn.guild.rite;

public final class RiteBreaks {

  private RiteBreaks() {}

  public record Hunter(boolean here, boolean hurt, double movedSq) {}

  public record Master(boolean here, boolean hurt, boolean fighting) {}

  public static boolean broken(
      final Hunter hunter, final Master master, final double breakDistance) {
    return !hunter.here()
        || hunter.hurt()
        || hunter.movedSq() > breakDistance * breakDistance
        || !master.here()
        || master.hurt()
        || master.fighting();
  }
}
