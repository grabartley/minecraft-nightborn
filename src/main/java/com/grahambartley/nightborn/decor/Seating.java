package com.grahambartley.nightborn.decor;

public final class Seating {

  public enum Choice {
    SIT,
    PASS,
    TAKEN
  }

  private Seating() {}

  public static Choice choose(final boolean sneaking, final boolean riding, final boolean taken) {
    if (sneaking || riding) {
      return Choice.PASS;
    }
    return taken ? Choice.TAKEN : Choice.SIT;
  }

  public static boolean keepsSeat(final boolean hasSitter, final boolean onChair) {
    return hasSitter && onChair;
  }
}
