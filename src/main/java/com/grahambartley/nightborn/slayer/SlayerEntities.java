package com.grahambartley.nightborn.slayer;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class SlayerEntities {

  private static final String THROWN_STAKE_ID = "thrown_stake";

  public static final EntityType<ThrownStakeEntity> THROWN_STAKE =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, THROWN_STAKE_ID),
          EntityType.Builder.<ThrownStakeEntity>create(ThrownStakeEntity::new, SpawnGroup.MISC)
              .dimensions(0.25F, 0.25F)
              .maxTrackingRange(4)
              .trackingTickInterval(10)
              .disableSaving()
              .disableSummon()
              .build(THROWN_STAKE_ID));

  private SlayerEntities() {}

  static void initialize() {}
}
