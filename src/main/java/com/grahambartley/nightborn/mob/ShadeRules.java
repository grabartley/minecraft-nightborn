package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.core.NightTime;

public final class ShadeRules {

  static final long DAWN_WARNING = 1000L;

  private ShadeRules() {}

  public static boolean sunIsNear(final boolean fixedTime, final long timeOfDay) {
    return !fixedTime
        && (!NightTime.isNight(false, timeOfDay)
            || !NightTime.isNight(false, timeOfDay + DAWN_WARNING));
  }

  public static boolean seeksShade(
      final boolean fixedTime, final long timeOfDay, final boolean openSky, final boolean wet) {
    return openSky && !wet && sunIsNear(fixedTime, timeOfDay);
  }
}
