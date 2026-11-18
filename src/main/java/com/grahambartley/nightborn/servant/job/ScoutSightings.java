package com.grahambartley.nightborn.servant.job;

import com.grahambartley.nightborn.downed.DownedTargeting;
import com.grahambartley.nightborn.vampire.Vampires;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;

public final class ScoutSightings {

  private ScoutSightings() {}

  public static ScoutReport around(final ServerWorld world, final Entity scout, final int sight) {
    final Box seen = scout.getBoundingBox().expand(sight);
    return ScoutReport.of(
        world
            .getEntitiesByClass(
                LivingEntity.class,
                seen,
                living ->
                    living != scout
                        && living.isAlive()
                        && !(living instanceof PlayerEntity player && player.isSpectator()))
            .stream()
            .map(ScoutSightings::sighting)
            .toList());
  }

  static ScoutSighting sighting(final LivingEntity living) {
    return ScoutSighting.of(
        living.getType().isIn(DownedTargeting.HUNTERS),
        Vampires.isVampire(living),
        living instanceof VillagerEntity,
        living instanceof PlayerEntity);
  }
}
