package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.hold.Holds;
import com.grahambartley.nightborn.trap.Grounding;
import net.minecraft.entity.LivingEntity;

public final class SlayerOpenings {

  private SlayerOpenings() {}

  public static boolean open(final boolean pinned, final boolean netted, final boolean opened) {
    return pinned || netted || opened;
  }

  public static boolean open(final LivingEntity target) {
    return open(Holds.pinned(target), Grounding.on(target), Holds.opened(target));
  }
}
