package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;

public final class StructureVisits {

  private StructureVisits() {}

  static void visit(final ServerPlayerEntity player, final Optional<FogZone> zone) {
    if (inside(zone, player.getX(), player.getY(), player.getZ())) {
      MilestoneCriterion.reach(player, Milestone.STRUCTURE_ENTERED);
    }
  }

  static boolean inside(
      final Optional<FogZone> zone, final double x, final double y, final double z) {
    return zone.isPresent() && zone.get().encloses(x, y, z);
  }
}
