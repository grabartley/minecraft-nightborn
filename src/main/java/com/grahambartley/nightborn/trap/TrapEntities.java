package com.grahambartley.nightborn.trap;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class TrapEntities {

  private static final String TRAP_ID = "trap";
  private static final String NET_ID = "net";

  public static final EntityType<TrapEntity> TRAP =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, TRAP_ID),
          EntityType.Builder.<TrapEntity>create(TrapEntity::new, SpawnGroup.MISC)
              .dimensions(0.5F, 0.1F)
              .maxTrackingRange(8)
              .disableSaving()
              .disableSummon()
              .makeFireImmune()
              .build(TRAP_ID));

  public static final EntityType<NetEntity> NET =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, NET_ID),
          EntityType.Builder.<NetEntity>create(NetEntity::new, SpawnGroup.MISC)
              .dimensions(0.5F, 0.5F)
              .maxTrackingRange(4)
              .trackingTickInterval(10)
              .disableSaving()
              .disableSummon()
              .build(NET_ID));

  private TrapEntities() {}

  static void initialize() {}
}
