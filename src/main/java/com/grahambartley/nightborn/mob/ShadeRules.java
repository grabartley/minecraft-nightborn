package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.core.NightTime;
import com.grahambartley.nightborn.sun.DirectSun;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;

public final class ShadeRules {

  static final long DAWN_WARNING = 1000L;

  private ShadeRules() {}

  public static boolean sunIsNear(final boolean fixedTime, final long timeOfDay) {
    return !fixedTime
        && (!NightTime.isNight(false, timeOfDay)
            || !NightTime.isNight(false, timeOfDay + DAWN_WARNING));
  }

  public static long ticksUntilSunIsNear(final long timeOfDay) {
    return Math.floorMod(NightTime.DAWN - DAWN_WARNING - timeOfDay, NightTime.DAY_LENGTH);
  }

  public static boolean endangered(final LivingEntity vampire) {
    final World world = vampire.getWorld();
    return DirectSun.under(
        vampire, sunIsNear(world.getDimension().hasFixedTime(), world.getTimeOfDay()));
  }
}
