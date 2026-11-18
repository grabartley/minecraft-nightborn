package com.grahambartley.nightborn.sun;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.TravelLog;
import com.grahambartley.nightborn.vampire.Vampires;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ParasolWalks {

  private static final TravelLog WALKS = new TravelLog(DeedKinds.PARASOL_WALKED);

  private ParasolWalks() {}

  public static void step(final LivingEntity holder) {
    if (holder instanceof ServerPlayerEntity player
        && Vampires.isVampire(player)
        && WALKS.step(player) > 0) {
      MilestoneCriterion.reach(player, Milestone.PARASOL_SHADE);
    }
  }

  public static void forget(final UUID player) {
    WALKS.forget(player);
  }
}
