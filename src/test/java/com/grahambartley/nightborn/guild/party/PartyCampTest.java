package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PartyCampTest {

  static Stream<Arguments> fires() {
    return Stream.of(
        Arguments.of(
            "an unlit campfire",
            Blocks.CAMPFIRE.getDefaultState().with(CampfireBlock.LIT, false),
            true),
        Arguments.of("a lit campfire", Blocks.CAMPFIRE.getDefaultState(), true),
        Arguments.of(
            "a drowned campfire",
            Blocks.CAMPFIRE
                .getDefaultState()
                .with(CampfireBlock.LIT, false)
                .with(CampfireBlock.WATERLOGGED, true),
            false),
        Arguments.of("a soul campfire", Blocks.SOUL_CAMPFIRE.getDefaultState(), false),
        Arguments.of("air", Blocks.AIR.getDefaultState(), false),
        Arguments.of("cobblestone", Blocks.COBBLESTONE.getDefaultState(), false));
  }

  @ParameterizedTest(name = "{0} can be the heart of the camp again: {2}")
  @MethodSource("fires")
  void setsUpAgainOnlyAroundItsOwnKindOfFireOnDryGround(
      final String name, final BlockState fire, final boolean reusable) {
    assertEquals(reusable, PartyCamp.reusable(fire));
  }
}
