package com.grahambartley.nightborn.age;

import com.grahambartley.nightborn.mob.stage.Staged;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class StageRanks {

  private StageRanks() {}

  public static int of(final LivingEntity vampire) {
    if (vampire instanceof Staged staged) {
      return staged.stageRank();
    }
    if (vampire instanceof PlayerEntity player && Vampires.isVampire(player)) {
      return AgeLadder.of(player.getRegistryManager()).stageRank(AgeAttachment.get(player).rank());
    }
    return 0;
  }
}
