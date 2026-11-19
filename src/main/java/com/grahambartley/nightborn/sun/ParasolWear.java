package com.grahambartley.nightborn.sun;

public final class ParasolWear {

  public static final int DURABILITY = 180;

  private ParasolWear() {}

  public static int points(final long sunTicks, final int lastsSeconds) {
    if (sunTicks <= 0) {
      return 0;
    }
    return (int) (worn(sunTicks, lastsSeconds) - worn(sunTicks - 1, lastsSeconds));
  }

  private static long worn(final long sunTicks, final int lastsSeconds) {
    return sunTicks * DURABILITY / (Math.max(1L, lastsSeconds) * 20L);
  }
}
