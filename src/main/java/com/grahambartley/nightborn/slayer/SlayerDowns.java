package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.age.StageRanks;
import com.grahambartley.nightborn.downed.Downed;
import com.grahambartley.nightborn.mob.stage.VampireStage;
import com.grahambartley.nightborn.mob.stage.VampireStages;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.skill.PowerReach;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public final class SlayerDowns {

  private SlayerDowns() {}

  public static boolean downs(
      final boolean vampire,
      final boolean boss,
      final boolean reachable,
      final boolean downed,
      final float health,
      final float maxHealth,
      final float below,
      final int stageRank,
      final int mostRank) {
    return vampire
        && !boss
        && reachable
        && !downed
        && health > 0.0F
        && health < maxHealth * below
        && stageRank <= mostRank;
  }

  public static boolean qualifies(
      final LivingEntity hunter, final LivingEntity target, final DownBlow blow) {
    return downs(
        Vampires.isVampire(target),
        !PowerReach.marks(target),
        PowerReach.touches(hunter, target),
        Downed.helpless(target),
        target.getHealth(),
        target.getMaxHealth(),
        blow.below(),
        StageRanks.of(target),
        VampireStages.stage(target.getWorld().getRegistryManager(), blow.upTo())
            .map(VampireStage::rank)
            .orElse(-1));
  }

  public static boolean down(
      final LivingEntity hunter, final LivingEntity target, final DownBlow blow) {
    if (!qualifies(hunter, target, blow)) {
      return false;
    }
    Downed.down(target, ServerConfigService.current().downed(), Optional.of(hunter.getUuid()));
    return true;
  }
}
