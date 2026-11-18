package com.grahambartley.nightborn.structure;

import com.grahambartley.nightborn.advancement.Milestone;
import com.grahambartley.nightborn.advancement.MilestoneCriterion;
import com.grahambartley.nightborn.challenge.Deed;
import com.grahambartley.nightborn.challenge.DeedKinds;
import com.grahambartley.nightborn.challenge.Deeds;
import java.util.Optional;
import net.minecraft.server.network.ServerPlayerEntity;

public final class StructureVisits {

  private StructureVisits() {}

  static void visit(final ServerPlayerEntity player, final Optional<StructureZone> zone) {
    if (inside(zone.map(StructureZone::area), player.getX(), player.getY(), player.getZ())) {
      MilestoneCriterion.reach(player, Milestone.STRUCTURE_ENTERED);
      Deeds.record(
          player,
          Deed.of(DeedKinds.STRUCTURE_ENTERED).with(DeedKinds.STRUCTURE, zone.get().structure()));
    }
  }

  static boolean inside(
      final Optional<FogZone> zone, final double x, final double y, final double z) {
    return zone.isPresent() && zone.get().encloses(x, y, z);
  }
}
