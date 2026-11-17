package com.grahambartley.nightborn.mob;

import com.grahambartley.nightborn.core.NightTime;
import net.minecraft.world.World;

public final class Hunting {

  private Hunting() {}

  public static boolean vampireHunts(final World world) {
    return NightTime.isNight(world.getDimension().hasFixedTime(), world.getTimeOfDay());
  }

  public static boolean batHunts(
      final boolean peaceful,
      final boolean fixedTime,
      final long timeOfDay,
      final boolean openSky) {
    return !peaceful && (!openSky || NightTime.isNight(fixedTime, timeOfDay));
  }

  public static boolean batSearches(final boolean hasPrey, final int age, final int interval) {
    return !hasPrey && age % interval == 0;
  }

  public static boolean batSwoops(final boolean hasPrey, final int biteCooldown) {
    return hasPrey && biteCooldown == 0;
  }

  public record Pursuit(
      boolean huntingHours,
      boolean hungry,
      boolean endangered,
      boolean provoked,
      boolean outOfRange) {}

  public static boolean dropsTarget(final Pursuit pursuit) {
    return pursuit.endangered()
        || pursuit.outOfRange()
        || (!pursuit.provoked() && (!pursuit.huntingHours() || !pursuit.hungry()));
  }
}
