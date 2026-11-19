package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import net.minecraft.block.BlockState;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class TentBlocksTest {

  @ParameterizedTest(name = "{0}")
  @EnumSource(TentPart.class)
  @DisplayName("no part of a tent drops anything, is an item, or can be pushed")
  void tentCannotBeTaken(final TentPart part) {
    final BlockState state =
        TentBlocks.HUNTER_TENT.getDefaultState().with(HunterTentBlock.PART, part);
    assertEquals(LootTables.EMPTY, TentBlocks.HUNTER_TENT.getLootTableKey());
    assertEquals(Items.AIR, TentBlocks.HUNTER_TENT.asItem());
    assertEquals(PistonBehavior.BLOCK, state.getPistonBehavior());
    assertFalse(state.isOpaque());
  }
}
