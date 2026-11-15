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

  public record Pursuit(
      boolean huntingHours,
      boolean hungry,
      boolean endangered,
      boolean provoked,
      boolean outOfRange) {}

  public static boolean huntsPrey(
      final boolean huntingHours,
      final boolean hungry,
      final boolean endangered,
      final boolean outOfRange) {
    return huntingHours && hungry && !endangered && !outOfRange;
  }

  public static boolean dropsTarget(final Pursuit pursuit) {
    return pursuit.endangered()
        || pursuit.outOfRange()
        || (!pursuit.provoked() && (!pursuit.huntingHours() || !pursuit.hungry()));
  }
}
