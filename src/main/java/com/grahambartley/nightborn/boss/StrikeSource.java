package com.grahambartley.nightborn.boss;

import java.util.Optional;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;

public final class StrikeSource {

  private StrikeSource() {}

  public static DamageSource of(
      final ServerWorld world,
      final MobEntity boss,
      final Optional<RegistryKey<DamageType>> damageType) {
    return damageType
        .map(type -> world.getDamageSources().create(type, boss))
        .orElseGet(() -> boss.getDamageSources().mobAttack(boss));
  }
}
