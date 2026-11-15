package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.ConstantIntProvider;

public final class GearBlocks {

  public static final Block SILVER_ORE = ore("silver_ore", Blocks.IRON_ORE);
  public static final Block DEEPSLATE_SILVER_ORE =
      ore("deepslate_silver_ore", Blocks.DEEPSLATE_IRON_ORE);

  public static final Block GARLIC =
      Registry.register(
          Registries.BLOCK,
          Identifier.of(Nightborn.MOD_ID, "garlic"),
          new GarlicCropBlock(AbstractBlock.Settings.copy(Blocks.CARROTS)));
  public static final Block GARLIC_STRING =
      withItem(
          "garlic_string",
          new GarlicStringBlock(
              AbstractBlock.Settings.create()
                  .mapColor(MapColor.PALE_YELLOW)
                  .noCollision()
                  .strength(0.2F)
                  .sounds(BlockSoundGroup.CROP)
                  .pistonBehavior(PistonBehavior.DESTROY)));

  private GearBlocks() {}

  public static void initialize() {}

  private static Block ore(final String name, final Block like) {
    return withItem(
        name,
        new ExperienceDroppingBlock(
            ConstantIntProvider.create(0), AbstractBlock.Settings.copy(like)));
  }

  private static Block withItem(final String name, final Block block) {
    final Identifier id = Identifier.of(Nightborn.MOD_ID, name);
    Registry.register(Registries.BLOCK, id, block);
    Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
    return block;
  }
}
