package com.grahambartley.nightborn.coffin;

import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public enum CoffinWood {
  OAK(Blocks.OAK_PLANKS),
  SPRUCE(Blocks.SPRUCE_PLANKS),
  DARK_OAK(Blocks.DARK_OAK_PLANKS),
  MANGROVE(Blocks.MANGROVE_PLANKS),
  CHERRY(Blocks.CHERRY_PLANKS),
  CRIMSON(Blocks.CRIMSON_PLANKS);

  private final Block planks;

  CoffinWood(final Block planks) {
    this.planks = planks;
  }

  public String id() {
    return this.name().toLowerCase(Locale.ROOT) + "_coffin";
  }

  public Block planks() {
    return this.planks;
  }
}
