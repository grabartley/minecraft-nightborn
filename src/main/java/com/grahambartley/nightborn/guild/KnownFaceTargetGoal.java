package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.village.VillageSuspicion;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class KnownFaceTargetGoal extends ActiveTargetGoal<PlayerEntity> {

  public KnownFaceTargetGoal(final HunterEntity hunter) {
    super(hunter, PlayerEntity.class, true, target -> knownTo(hunter, target));
  }

  static boolean knownTo(final HunterEntity hunter, final LivingEntity target) {
    return hunter.getWorld() instanceof ServerWorld world
        && guarded(world, hunter.partyLeader())
            .filter(bell -> VillageSuspicion.knows(world, bell, target.getUuid()))
            .isPresent();
  }

  private static Optional<BlockPos> guarded(final ServerWorld world, final Optional<UUID> leader) {
    return leader
        .map(world::getEntity)
        .filter(GuildMasterEntity.class::isInstance)
        .map(GuildMasterEntity.class::cast)
        .filter(master -> master.party() != null)
        .flatMap(master -> master.party().guards());
  }
}
