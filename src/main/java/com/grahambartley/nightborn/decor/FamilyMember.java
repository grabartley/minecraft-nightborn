package com.grahambartley.nightborn.decor;

import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.WallBlock;

public enum FamilyMember {
  BASE("", (base, settings) -> new Block(settings)),
  STAIRS("_stairs", (base, settings) -> new StairsBlock(base.getDefaultState(), settings)),
  SLAB("_slab", (base, settings) -> new SlabBlock(settings)),
  WALL("_wall", (base, settings) -> new WallBlock(settings.solid())),
  FENCE("_fence", (base, settings) -> new FenceBlock(settings));

  private record Stem(String baseSuffix, String memberSuffix) {}

  private static final List<Stem> STEMS =
      List.of(new Stem("_bricks", "_brick"), new Stem("_tiles", "_tile"), new Stem("_planks", ""));

  private final String suffix;
  private final BiFunction<Block, AbstractBlock.Settings, Block> factory;

  FamilyMember(
      final String suffix, final BiFunction<Block, AbstractBlock.Settings, Block> factory) {
    this.suffix = suffix;
    this.factory = factory;
  }

  public String idFor(final String baseId) {
    if (this == BASE) {
      return baseId;
    }
    for (final Stem stem : STEMS) {
      if (baseId.endsWith(stem.baseSuffix())) {
        return baseId.substring(0, baseId.length() - stem.baseSuffix().length())
            + stem.memberSuffix()
            + this.suffix;
      }
    }
    return baseId + this.suffix;
  }

  public Block create(final Block base, final AbstractBlock.Settings settings) {
    return this.factory.apply(base, settings);
  }
}
