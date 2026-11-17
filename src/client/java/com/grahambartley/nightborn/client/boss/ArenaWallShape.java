package com.grahambartley.nightborn.client.boss;

public final class ArenaWallShape {

  static final double SEGMENT = 1.5;
  static final int FEWEST = 16;
  static final double SIGHT = 12.0;
  static final float FAINTEST = 0.15F;
  static final float STRONGEST = 0.75F;

  private ArenaWallShape() {}

  public static int segments(final int radius) {
    return Math.max(FEWEST, (int) Math.ceil(2.0 * Math.PI * radius / SEGMENT));
  }

  public static double x(final int segment, final int segments, final int radius) {
    return Math.cos(2.0 * Math.PI * segment / segments) * radius;
  }

  public static double z(final int segment, final int segments, final int radius) {
    return Math.sin(2.0 * Math.PI * segment / segments) * radius;
  }

  public static float alpha(final double fromWall) {
    final double near = Math.max(0.0, Math.min(1.0, 1.0 - Math.abs(fromWall) / SIGHT));
    return (float) (FAINTEST + (STRONGEST - FAINTEST) * near);
  }
}
