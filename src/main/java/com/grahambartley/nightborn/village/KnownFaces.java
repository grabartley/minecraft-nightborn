package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.feeding.VillagerWitnesses;
import com.grahambartley.nightborn.mind.MindTies;
import com.grahambartley.nightborn.server.ServerConfigService;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class KnownFaces {

  private KnownFaces() {}

  public static boolean shuns(final boolean known, final boolean charmedByThem) {
    return known && !charmedByThem;
  }

  static void shun(final ServerWorld world, final BlockPos bell, final VillageRecord record) {
    if (record.faces().isEmpty()) {
      return;
    }
    final double reach = VillageSuspicion.settings().villageReach();
    final int range = ServerConfigService.current().feeding().witnessRange();
    for (final ServerPlayerEntity player : world.getPlayers()) {
      if (player.isSpectator()
          || !record.knows(player.getUuid())
          || player.getBlockPos().getSquaredDistance(bell) > reach * reach) {
        continue;
      }
      final List<VillagerEntity> seeing = VillagerWitnesses.witnesses(world, player, null, range);
      for (final VillagerEntity villager : seeing) {
        if (VillageSuspicion.bellOf(villager).equals(Optional.of(bell))
            && shuns(true, MindTies.charmedBy(villager, player))) {
          VillagerWitnesses.panic(villager, player, world.getDamageSources().playerAttack(player));
        }
      }
    }
  }
}
