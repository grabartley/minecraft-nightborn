package com.grahambartley.nightborn.guild;

import com.grahambartley.nightborn.vampire.Vampires;
import com.grahambartley.nightborn.village.VillageSuspicion;
import java.util.Optional;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class KnownFaceTargetGoal extends ActiveTargetGoal<PlayerEntity> {

  public <T extends MobEntity & GuildFighter> KnownFaceTargetGoal(final T hunter) {
    super(hunter, PlayerEntity.class, true, target -> knownTo(hunter, target));
  }

  public static boolean hunts(final boolean knownThere, final boolean vampire) {
    return knownThere && vampire;
  }

  static <T extends MobEntity & GuildFighter> boolean knownTo(
      final T hunter, final LivingEntity target) {
    return hunter.getWorld() instanceof ServerWorld world
        && hunts(
            guarded(hunter.master())
                .filter(bell -> VillageSuspicion.knows(world, bell, target.getUuid()))
                .isPresent(),
            Vampires.isVampire(target));
  }

  private static Optional<BlockPos> guarded(final Optional<GuildMasterEntity> master) {
    return master
        .filter(leader -> leader.party() != null)
        .flatMap(leader -> leader.party().guards());
  }
}
