package com.grahambartley.nightborn.guild.party;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class TentBlocks {

  static final Identifier HUNTER_TENT_ID = Identifier.of(Nightborn.MOD_ID, "hunter_tent");

  public static final Block HUNTER_TENT =
      Registry.register(
          Registries.BLOCK,
          HUNTER_TENT_ID,
          new HunterTentBlock(
              AbstractBlock.Settings.copy(Blocks.BROWN_WOOL)
                  .dropsNothing()
                  .nonOpaque()
                  .suffocates((state, world, pos) -> false)
                  .blockVision((state, world, pos) -> false)
                  .allowsSpawning((state, world, pos, type) -> false)
                  .pistonBehavior(PistonBehavior.BLOCK)));

  public static final BlockEntityType<HunterTentBlockEntity> HUNTER_TENT_ENTITY =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          HUNTER_TENT_ID,
          BlockEntityType.Builder.create(HunterTentBlockEntity::new, HUNTER_TENT).build());

  private TentBlocks() {}

  public static void register() {}
}
