package com.grahambartley.nightborn.feeding;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public interface BiteQuarry {

  boolean bitable();

  boolean struggles();

  boolean finishable();

  static boolean guards(final LivingEntity victim) {
    return guards(victim instanceof BiteQuarry quarry ? quarry : null);
  }

  static boolean struggles(final LivingEntity victim) {
    return struggles(victim instanceof BiteQuarry quarry ? quarry : null);
  }

  static boolean finishable(final LivingEntity victim) {
    return finishable(victim instanceof BiteQuarry quarry ? quarry : null);
  }

  static boolean guards(@Nullable final BiteQuarry quarry) {
    return quarry != null && !quarry.bitable();
  }

  static boolean struggles(@Nullable final BiteQuarry quarry) {
    return quarry == null || quarry.struggles();
  }

  static boolean finishable(@Nullable final BiteQuarry quarry) {
    return quarry == null || quarry.finishable();
  }
}
