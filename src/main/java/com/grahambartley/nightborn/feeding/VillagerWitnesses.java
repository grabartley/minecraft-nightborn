package com.grahambartley.nightborn.feeding;

import java.util.List;
import java.util.Optional;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.GlobalPos;

public final class VillagerWitnesses {

  static final int PANIC_TICKS = 200;

  private VillagerWitnesses() {}

  public static void alert(
      final ServerWorld world,
      final LivingEntity vampire,
      final LivingEntity victim,
      final FeedingSession session,
      final int range,
      final DamageSource bite) {
    if (range <= 0) {
      return;
    }
    final List<VillagerEntity> witnesses =
        world.getEntitiesByClass(
            VillagerEntity.class,
            Box.of(vampire.getPos(), range * 2.0, range * 2.0, range * 2.0),
            villager -> sees(villager, vampire, victim, range));
    for (final VillagerEntity witness : witnesses) {
      if (session.firstSighting(witness.getUuid())) {
        witness.onInteractionWith(EntityInteraction.VILLAGER_HURT, vampire);
        witness.getBrain().remember(MemoryModuleType.HURT_BY, bite, PANIC_TICKS);
        witness.getBrain().remember(MemoryModuleType.HURT_BY_ENTITY, vampire, PANIC_TICKS);
        if (!session.bellRung() && ringBell(world, witness)) {
          session.markBellRung();
        }
      }
    }
  }

  private static boolean sees(
      final VillagerEntity villager,
      final LivingEntity vampire,
      final LivingEntity victim,
      final int range) {
    return villager != victim
        && villager.isAlive()
        && !villager.isSleeping()
        && villager.squaredDistanceTo(vampire) <= (double) range * range
        && villager.canSee(vampire);
  }

  private static boolean ringBell(final ServerWorld world, final VillagerEntity witness) {
    final Optional<GlobalPos> meetingPoint =
        witness.getBrain().getOptionalRegisteredMemory(MemoryModuleType.MEETING_POINT);
    if (meetingPoint.isEmpty() || meetingPoint.get().dimension() != world.getRegistryKey()) {
      return false;
    }
    final BlockPos pos = meetingPoint.get().pos();
    if (!world.isChunkLoaded(pos)) {
      return false;
    }
    final BlockState state = world.getBlockState(pos);
    return state.getBlock() instanceof BellBlock bell && bell.ring(witness, world, pos, null);
  }
}
