package com.grahambartley.nightborn.gear;

import com.grahambartley.nightborn.Nightborn;
import com.grahambartley.nightborn.decor.WallDecorBlock;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
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
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class GearBlocks {

  private static final VoxelShape CRUCIFIX_SHAPE =
      VoxelShapes.union(
          Block.createCuboidShape(7, 1, 14, 9, 15, 16),
          Block.createCuboidShape(4, 9, 14, 12, 11, 16));

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

  public static final Block GOLD_WALL_CRUCIFIX = wallCrucifix("gold_wall_crucifix", MapColor.GOLD);
  public static final Block SILVER_WALL_CRUCIFIX =
      wallCrucifix("silver_wall_crucifix", MapColor.IRON_GRAY);

  static final int STRING_BURN = 30;
  static final int STRING_SPREAD = 60;

  private GearBlocks() {}

  public static void initialize() {
    FlammableBlockRegistry.getDefaultInstance().add(GARLIC_STRING, STRING_BURN, STRING_SPREAD);
  }

  private static Block wallCrucifix(final String name, final MapColor color) {
    return Registry.register(
        Registries.BLOCK,
        Identifier.of(Nightborn.MOD_ID, name),
        new WallDecorBlock(
            AbstractBlock.Settings.create()
                .mapColor(color)
                .noCollision()
                .strength(0.5F)
                .sounds(BlockSoundGroup.METAL)
                .pistonBehavior(PistonBehavior.DESTROY),
            CRUCIFIX_SHAPE));
  }

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
