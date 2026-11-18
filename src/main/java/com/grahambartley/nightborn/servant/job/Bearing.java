package com.grahambartley.nightborn.servant.job;

import java.util.Locale;

public enum Bearing {
  SOUTH,
  SOUTH_WEST,
  WEST,
  NORTH_WEST,
  NORTH,
  NORTH_EAST,
  EAST,
  SOUTH_EAST;

  public static Bearing of(final double dx, final double dz) {
    final double degrees = Math.toDegrees(Math.atan2(-dx, dz));
    final int sector = (int) Math.floorMod(Math.round(degrees / 45.0), values().length);
    return values()[sector];
  }

  public String translationKey() {
    return "nightborn.bearing." + name().toLowerCase(Locale.ROOT);
  }
}
