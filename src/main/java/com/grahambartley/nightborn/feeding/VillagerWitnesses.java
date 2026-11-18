package com.grahambartley.nightborn.feeding;

import com.grahambartley.nightborn.clothing.Noticing;
import com.grahambartley.nightborn.village.SuspicionCause;
import com.grahambartley.nightborn.village.WitnessReports;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.entity.EntityInteraction;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import org.jetbrains.annotations.Nullable;

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
    final List<VillagerEntity> witnesses = witnesses(world, vampire, victim, witnessRange);
    for (final VillagerEntity witness : witnesses) {
      if (session.firstSighting(witness.getUuid())) {
        witness.onInteractionWith(EntityInteraction.VILLAGER_HURT, vampire);
        panic(witness, vampire, bite);
        WitnessReports.saw(world, witness, vampire, SuspicionCause.SEEN_FEEDING);
      }
    }
    return !witnesses.isEmpty();
  }

  public static List<VillagerEntity> witnesses(
      final ServerWorld world,
      final LivingEntity vampire,
      @Nullable final LivingEntity victim,
      final int witnessRange) {
    final int range = Noticing.byVillagers(vampire, witnessRange);
    if (range <= 0) {
      return List.of();
    }
    return world.getEntitiesByClass(
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
  }

  public static void panic(
      final VillagerEntity witness, final LivingEntity vampire, final DamageSource cause) {
    witness.getBrain().remember(MemoryModuleType.HURT_BY, cause, PANIC_TICKS);
    witness.getBrain().remember(MemoryModuleType.HURT_BY_ENTITY, vampire, PANIC_TICKS);
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
}
