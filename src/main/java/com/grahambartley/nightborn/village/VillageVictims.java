package com.grahambartley.nightborn.village;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;

public final class VillageVictims {

  private VillageVictims() {}

  public static Optional<SuspicionCause> found(
      final boolean villager, final boolean animal, final boolean survived) {
    if (villager) {
      return Optional.of(
          survived ? SuspicionCause.BITTEN_VILLAGER : SuspicionCause.MISSING_VILLAGER);
    }
    return animal && !survived ? Optional.of(SuspicionCause.DRAINED_ANIMAL) : Optional.empty();
  }

  public static void bitten(final ServerWorld world, final LivingEntity victim) {
    found(victim instanceof VillagerEntity, victim instanceof AnimalEntity, victim.isAlive())
        .ifPresent(cause -> VillageSuspicion.victim(world, victim, cause));
  }
}
