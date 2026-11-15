package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;

public final class GearBlocks {

  public static final Block SILVER_ORE = ore("silver_ore", Blocks.IRON_ORE);
  public static final Block DEEPSLATE_SILVER_ORE =
      ore("deepslate_silver_ore", Blocks.DEEPSLATE_IRON_ORE);

  private GearBlocks() {}

  public static void initialize() {}

  private static Block ore(final String name, final Block like) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, name);
    final Block block =
        Registry.register(
            Registries.BLOCK,
            id,
            new ExperienceDroppingBlock(
                ConstantIntProvider.create(0), AbstractBlock.Settings.copy(like)));
    Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
    return block;
  }
}
