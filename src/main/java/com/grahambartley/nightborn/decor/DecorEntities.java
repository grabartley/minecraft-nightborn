package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class DecorEntities {

  private static final String SEAT_ID = "seat";
  private static final float SEAT_SIZE = 0.01f;

  public static final EntityType<SeatEntity> SEAT =
      Registry.register(
          Registries.ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, SEAT_ID),
          EntityType.Builder.<SeatEntity>create(SeatEntity::new, SpawnGroup.MISC)
              .dimensions(SEAT_SIZE, SEAT_SIZE)
              .disableSaving()
              .disableSummon()
              .makeFireImmune()
              .build(SEAT_ID));

  private DecorEntities() {}

  public static void initialize() {}
}
