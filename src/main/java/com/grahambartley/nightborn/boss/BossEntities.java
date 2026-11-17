package com.grahambartley.nightborn.boss;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class BossEntities {

  private static final String ZONE_MARK_ID = "zone_mark";

  public static final EntityType<ZoneMarkEntity> ZONE_MARK =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, ZONE_MARK_ID),
          EntityType.Builder.<ZoneMarkEntity>create(ZoneMarkEntity::new, SpawnGroup.MISC)
              .dimensions(0.5F, 0.1F)
              .maxTrackingRange(8)
              .disableSaving()
              .disableSummon()
              .makeFireImmune()
              .build(ZONE_MARK_ID));

  private BossEntities() {}

  public static void initialize() {}
}
