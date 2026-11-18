package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.clothing.Noticing;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
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

  public static boolean alert(
      final ServerWorld world,
      final LivingEntity vampire,
      final LivingEntity victim,
      final FeedingSession session,
      final int witnessRange,
      final DamageSource bite) {
    final int range = Noticing.byVillagers(vampire, witnessRange);
    if (range <= 0) {
      return false;
    }
    final List<VillagerEntity> witnesses =
        world.getEntitiesByClass(
            VillagerEntity.class,
            Box.of(vampire.getPos(), range * 2.0, range * 2.0, range * 2.0),
            villager ->
                sees(
                    villager == victim,
                    villager.isAlive(),
                    villager.isSleeping(),
                    villager.squaredDistanceTo(vampire),
                    range,
                    () -> villager.canSee(vampire)));
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
    return !witnesses.isEmpty();
  }

  static boolean sees(
      final boolean victim,
      final boolean alive,
      final boolean sleeping,
      final double distanceSquared,
      final int range,
      final BooleanSupplier lineOfSight) {
    return !victim
        && alive
        && !sleeping
        && distanceSquared <= (double) range * range
        && lineOfSight.getAsBoolean();
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
