package com.grahambartley.nightborn.oldblood;

import com.grahambartley.nightborn.Nightborn;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class OldBloodBlocks {

  static final Identifier FONT_ID = Identifier.of(Nightborn.MOD_ID, "old_blood_font");

  public static final Block FONT =
      Registry.register(
          Registries.BLOCK,
          FONT_ID,
          new OldBloodFontBlock(
              AbstractBlock.Settings.copy(Blocks.POLISHED_BLACKSTONE_BRICKS)
                  .strength(-1.0F, 3_600_000.0F)
                  .dropsNothing()
                  .nonOpaque()
                  .pistonBehavior(PistonBehavior.BLOCK)));

  public static final BlockEntityType<OldBloodFontBlockEntity> FONT_ENTITY =
      Registry.register(
          Registries.BLOCK_ENTITY_TYPE,
          FONT_ID,
          BlockEntityType.Builder.create(OldBloodFontBlockEntity::new, FONT).build());

  private OldBloodBlocks() {}

  public static void register() {}
}
