package com.grahambartley.nightborn.accessory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public final class FrostWard {

  private FrostWard() {}

  public static boolean warded(final Entity entity) {
    return entity instanceof LivingEntity living
        && warded(GiftAttributes.value(living, GiftAttributes.FROST_WARD));
  }

  public static boolean warded(final double ward) {
    return ward > 0.0;
  }
}
