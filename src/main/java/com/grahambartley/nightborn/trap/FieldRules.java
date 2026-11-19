package com.grahambartley.nightborn.trap;

public final class FieldRules {

  private FieldRules() {}

  public static boolean bars(
      final boolean listed, final boolean vampire, final boolean inside, final boolean reaches) {
    return listed && vampire && inside && reaches;
  }

  public static boolean inside(final double squaredDistance, final float radius) {
    return squaredDistance <= (double) radius * radius;
  }

  public static boolean lasts(final long now, final long until) {
    return now < until;
  }
}
