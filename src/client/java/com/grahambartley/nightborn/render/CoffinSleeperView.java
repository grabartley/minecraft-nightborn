package com.grahambartley.nightborn.render;

import com.grahambartley.nightborn.coffin.CoffinBlockEntity;
import com.grahambartley.nightborn.coffin.CoffinSleepers;
import net.minecraft.entity.LivingEntity;

public final class CoffinSleeperView {

  private CoffinSleeperView() {}

  public static boolean hidden(final LivingEntity sleeper) {
    return sleeper
        .getSleepingPosition()
        .map(
            pos ->
                hides(
                    CoffinSleepers.shelters(sleeper.getWorld().getBlockState(pos)),
                    sleeper.getWorld().getBlockEntity(pos) instanceof CoffinBlockEntity coffin
                        ? coffin.openness(1.0F)
                        : 1.0F))
        .orElse(false);
  }

  public static float cameraLift(final boolean inCoffin, final float lift) {
    return inCoffin ? 0.0F : lift;
  }

  static boolean hides(final boolean shut, final float openness) {
    return shut && openness <= 0.0F;
  }
}
