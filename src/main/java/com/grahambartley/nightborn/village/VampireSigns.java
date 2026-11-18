package com.grahambartley.nightborn.village;

import com.grahambartley.nightborn.feeding.VillagerWitnesses;
import com.grahambartley.nightborn.server.ServerConfigService;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class VampireSigns {

  private VampireSigns() {}

  public static void shown(final LivingEntity vampire, final SuspicionCause cause) {
    if (!(vampire instanceof ServerPlayerEntity player) || !Vampires.isVampire(player)) {
      return;
    }
    final ServerWorld world = player.getServerWorld();
    for (final VillagerEntity witness :
        VillagerWitnesses.witnesses(
            world, player, null, ServerConfigService.current().feeding().witnessRange())) {
      if (WitnessReports.saw(world, witness, player, cause)) {
        VillagerWitnesses.panic(witness, player, world.getDamageSources().playerAttack(player));
      }
    }
  }
}
