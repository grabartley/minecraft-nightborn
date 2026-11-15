package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.core.NightTime;
import net.minecraft.util.math.MathHelper;

public final class DaySkip {

  private DaySkip() {}

  public static int required(final int vampires, final int sharePercent) {
    return Math.max(1, MathHelper.ceil(vampires * sharePercent / 100.0F));
  }

  public static boolean skips(final int resting, final int vampires, final int sharePercent) {
    return vampires > 0 && resting >= required(vampires, sharePercent);
  }

  public static long nextDusk(final long timeOfDay) {
    final long dusk = timeOfDay - Math.floorMod(timeOfDay, NightTime.DAY_LENGTH) + NightTime.DUSK;
    return dusk > timeOfDay ? dusk : dusk + NightTime.DAY_LENGTH;
  }
}
