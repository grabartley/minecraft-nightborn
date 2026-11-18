package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class RestlessDead {

  public static final EntityType<RestlessDeadEntity> TYPE =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, "restless_dead"),
          EntityType.Builder.create(RestlessDeadEntity::new, SpawnGroup.MISC)
              .dimensions(0.6F, 1.95F)
              .eyeHeight(1.74F)
              .maxTrackingRange(8)
              .build());

  private RestlessDead() {}

  static void register() {
    FabricDefaultAttributeRegistry.register(TYPE, RestlessDeadEntity.createRestlessAttributes());
  }
}
