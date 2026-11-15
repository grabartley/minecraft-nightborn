package com.grahambartley.nightborn.familiar;

import com.grahambartley.nightborn.core.NightTime;
import net.minecraft.world.World;

public final class FamiliarClock {

  private FamiliarClock() {}

  public static long today(final World world) {
    return dayOf(world.getTimeOfDay());
  }

  public static long dayOf(final long timeOfDay) {
    return Math.floorDiv(timeOfDay, NightTime.DAY_LENGTH);
  }

  public static boolean isWorkingHours(final World world) {
    return !NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay());
  }
}
