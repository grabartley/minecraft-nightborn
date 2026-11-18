package com.grahambartley.nightborn.raid;

public final class MapSpread {

  public static final int SMALLEST_HALF_WIDTH = 64;
  public static final int LARGEST_SCALE = 4;

  private MapSpread() {}

  public static byte scale(final int spread) {
    for (int scale = 0; scale < LARGEST_SCALE; scale++) {
      if (SMALLEST_HALF_WIDTH << scale >= spread) {
        return (byte) scale;
      }
    }
    return LARGEST_SCALE;
  }
}
