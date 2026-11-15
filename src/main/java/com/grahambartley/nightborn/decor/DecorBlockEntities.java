package com.grahambartley.nightborn.decor;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class DecorBlockEntities {

  public static final BlockEntityType<FurnitureBlockEntity> FURNITURE =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, "furniture"),
          BlockEntityType.Builder.create(
                  FurnitureBlockEntity::new,
                  DecorBlocks.all().stream()
                      .filter(AnimatedFurniture.class::isInstance)
                      .toArray(Block[]::new))
              .build());

  public static final BlockEntityType<WeaponRackBlockEntity> WEAPON_RACK =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          Identifier.of(Nightborn.MOD_ID, "weapon_rack"),
          BlockEntityType.Builder.create(WeaponRackBlockEntity::new, DecorBlocks.WEAPON_RACK)
              .build());

  private DecorBlockEntities() {}

  public static void initialize() {}
}
