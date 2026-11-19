package com.grahambartley.nightborn.occult;

import com.grahambartley.nightborn.Nightborn;
import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
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

  public static final RegistryKey<Registry<RestlessLook>> LOOK =
      RegistryKey.ofRegistry(Identifier.of(Nightborn.MOD_ID, "restless_dead_look"));

  private RestlessDead() {}

  static void register() {
    FabricDefaultAttributeRegistry.register(TYPE, RestlessDeadEntity.createRestlessAttributes());
    DynamicRegistries.registerSynced(LOOK, RestlessLook.CODEC);
  }

  public static Map<Identifier, RestlessLook> looks(final DynamicRegistryManager registries) {
    final Map<Identifier, RestlessLook> looks = new HashMap<>();
    registries
        .getOptional(LOOK)
        .ifPresent(registry -> registry.getIds().forEach(id -> looks.put(id, registry.get(id))));
    return looks;
  }
}
