package com.grahambartley.nightborn.tools.structures.vampire;

public final class RouteRoom {

  public static final double HOVER_HEIGHT = 1.0;
  public static final double TALLEST_HOUSEHOLD = 2.0;
  public static final double TALLEST_LORD = 2.3;
  public static final int HOUSEHOLD = headroom(TALLEST_HOUSEHOLD);
  public static final int LORD = headroom(TALLEST_LORD);
  public static final int LANES = 2;
  public static final int LONGEST_SQUEEZE = 3;

  private RouteRoom() {}

  public static int onFoot(final double height) {
    return (int) Math.floor(height + 1.0);
  }

  public static int floating(final double height) {
    return (int) Math.ceil(height + HOVER_HEIGHT);
  }

  public static int headroom(final double height) {
    return Math.max(onFoot(height), floating(height));
  }
}
