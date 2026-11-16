package com.grahambartley.nightborn.mob.household;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.decor.ChairBlock;
import com.grahambartley.nightborn.decor.DrapeBlock;
import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

public enum FurnitureKind {
  SEAT,
  BOOKS,
  LIGHT,
  DRAPE;

  public static final TagKey<Block> READING =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "vampire_reading"));
  public static final TagKey<Block> TENDED_LIGHTS =
      TagKey.of(RegistryKeys.BLOCK, Identifier.of(Nightborn.MOD_ID, "vampire_tended_lights"));

  public static Optional<FurnitureKind> of(final BlockState state) {
    if (state.getBlock() instanceof ChairBlock) {
      return Optional.of(SEAT);
    }
    if (state.getBlock() instanceof DrapeBlock) {
      return Optional.of(DRAPE);
    }
    if (state.isIn(READING)) {
      return Optional.of(BOOKS);
    }
    if (state.isIn(TENDED_LIGHTS) && state.contains(Properties.LIT)) {
      return Optional.of(LIGHT);
    }
    return Optional.empty();
  }
}
