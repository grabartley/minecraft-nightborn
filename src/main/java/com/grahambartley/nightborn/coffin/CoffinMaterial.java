package com.grahambartley.nightborn.coffin;

import com.grahambartley.nightborn.decor.DecorBlocks;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

public enum CoffinMaterial {
  OAK(CoffinStyle.WOOD, () -> Blocks.OAK_PLANKS),
  SPRUCE(CoffinStyle.WOOD, () -> Blocks.SPRUCE_PLANKS),
  DARK_OAK(CoffinStyle.WOOD, () -> Blocks.DARK_OAK_PLANKS),
  MANGROVE(CoffinStyle.WOOD, () -> Blocks.MANGROVE_PLANKS),
  CHERRY(CoffinStyle.WOOD, () -> Blocks.CHERRY_PLANKS),
  CRIMSON(CoffinStyle.WOOD, () -> Blocks.CRIMSON_PLANKS),
  STONE_BRICK(CoffinStyle.STONE, () -> Blocks.STONE_BRICKS),
  DEEPSLATE_BRICK(CoffinStyle.STONE, () -> Blocks.DEEPSLATE_BRICKS),
  POLISHED_BLACKSTONE_BRICK(CoffinStyle.STONE, () -> Blocks.POLISHED_BLACKSTONE_BRICKS),
  GOTHIC_STONE_BRICK(CoffinStyle.STONE, () -> DecorBlocks.get("gothic_stone_bricks"));

  private final CoffinStyle style;
  private final Supplier<Block> source;

  CoffinMaterial(final CoffinStyle style, final Supplier<Block> source) {
    this.style = style;
    this.source = source;
  }

  public String id() {
    return this.name().toLowerCase(Locale.ROOT) + "_coffin";
  }

  public CoffinStyle style() {
    return this.style;
  }

  public Block source() {
    return this.source.get();
  }
}
