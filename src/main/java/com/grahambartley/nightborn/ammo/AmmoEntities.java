package com.grahambartley.nightborn.ammo;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class AmmoEntities {

  public static final EntityType<BoltEntity> BOLT =
      register(
          "bolt",
          EntityType.Builder.<BoltEntity>create(BoltEntity::new, SpawnGroup.MISC)
              .dimensions(0.5F, 0.5F)
              .eyeHeight(0.13F)
              .maxTrackingRange(4)
              .trackingTickInterval(20));

  public static final EntityType<GarlicBombEntity> GARLIC_BOMB =
      register(
          "garlic_bomb",
          EntityType.Builder.<GarlicBombEntity>create(GarlicBombEntity::new, SpawnGroup.MISC)
              .dimensions(0.25F, 0.25F)
              .maxTrackingRange(4)
              .trackingTickInterval(10));

  private AmmoEntities() {}

  public static void initialize() {}

  private static <T extends Entity> EntityType<T> register(
      final String name, final EntityType.Builder<T> builder) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, name);
    return Registry.register(Registries.ENTITY_TYPE, id, builder.build(name));
  }
}
