package com.grahambartley.nightborn.feeding;

import net.minecraft.entity.LivingEntity;

public interface BiteQuarry {

  boolean bitable();

  boolean struggles();

  boolean finishable();

  static boolean guards(final LivingEntity victim) {
    return victim instanceof BiteQuarry quarry && !quarry.bitable();
  }

  static boolean struggles(final LivingEntity victim) {
    return !(victim instanceof BiteQuarry quarry) || quarry.struggles();
  }

  static boolean finishable(final LivingEntity victim) {
    return !(victim instanceof BiteQuarry quarry) || quarry.finishable();
  }
}
