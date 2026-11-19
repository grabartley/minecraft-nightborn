package com.grahambartley.nightborn.trap;

import java.util.Comparator;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public final class Traps {

  private Traps() {}

  public static void set(
      final LivingEntity caster,
      final Identifier skill,
      final TrapPower power,
      final BlockPos spot) {
    if (!(caster.getWorld() instanceof ServerWorld world)) {
      return;
    }
    final TrapLoadout loadout =
        TrapLoadout.of(power, TrapGifts.forPower(HeldTrapGifts.of(caster), skill));
    final List<TrapEntity> set =
        world
            .getEntitiesByType(TrapEntities.TRAP, trap -> trap.ownedBy(caster.getUuid(), skill))
            .stream()
            .map(TrapEntity.class::cast)
            .sorted(Comparator.comparingInt((TrapEntity trap) -> trap.age).reversed())
            .toList();
    set.stream()
        .limit(TrapRules.overLimit(set.size(), loadout.limit()))
        .forEach(TrapEntity::discard);
    TrapEntity.set(world, caster, skill, power, loadout, spot);
  }
}
