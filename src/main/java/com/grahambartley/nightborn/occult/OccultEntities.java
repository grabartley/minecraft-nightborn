package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class OccultEntities {

  private static final String BAT_SWARM_ID = "bat_swarm";

  public static final EntityType<BatSwarmEntity> BAT_SWARM =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, BAT_SWARM_ID),
          EntityType.Builder.<BatSwarmEntity>create(BatSwarmEntity::new, SpawnGroup.MISC)
              .dimensions(5.5F, 2.0F)
              .maxTrackingRange(8)
              .trackingTickInterval(1)
              .disableSaving()
              .disableSummon()
              .makeFireImmune()
              .build(BAT_SWARM_ID));

  private OccultEntities() {}

  static void initialize() {}
}
