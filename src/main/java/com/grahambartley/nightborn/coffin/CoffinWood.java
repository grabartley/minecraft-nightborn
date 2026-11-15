package com.grahambartley.nightborn.coffin;

import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public enum CoffinWood {
  OAK(Blocks.OAK_PLANKS, true),
  SPRUCE(Blocks.SPRUCE_PLANKS, true),
  DARK_OAK(Blocks.DARK_OAK_PLANKS, true),
  MANGROVE(Blocks.MANGROVE_PLANKS, true),
  CHERRY(Blocks.CHERRY_PLANKS, true),
  CRIMSON(Blocks.CRIMSON_PLANKS, false);

  private final Block planks;
  private final boolean flammable;

  CoffinWood(final Block planks, final boolean flammable) {
    this.planks = planks;
    this.flammable = flammable;
  }

  public String id() {
    return this.name().toLowerCase(Locale.ROOT) + "_coffin";
  }

  public Block planks() {
    return this.planks;
  }

  public boolean flammable() {
    return this.flammable;
  }
}
