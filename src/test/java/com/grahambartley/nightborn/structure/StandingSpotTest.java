package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StandingSpotTest {

  static Stream<Arguments> spots() {
    return Stream.of(
        Arguments.of(Blocks.STONE_BRICKS, Blocks.AIR, Blocks.AIR, true),
        Arguments.of(Blocks.DARK_OAK_PLANKS, Blocks.RED_CARPET, Blocks.AIR, true),
        Arguments.of(Blocks.STONE_BRICKS, Blocks.COBWEB, Blocks.AIR, true),
        Arguments.of(Blocks.AIR, Blocks.AIR, Blocks.AIR, false),
        Arguments.of(Blocks.WATER, Blocks.AIR, Blocks.AIR, false),
        Arguments.of(Blocks.LAVA, Blocks.AIR, Blocks.AIR, false),
        Arguments.of(Blocks.STONE_BRICKS, Blocks.STONE_BRICKS, Blocks.AIR, false),
        Arguments.of(Blocks.STONE_BRICKS, Blocks.AIR, Blocks.STONE_BRICKS, false),
        Arguments.of(Blocks.STONE_BRICKS, Blocks.WATER, Blocks.AIR, false),
        Arguments.of(Blocks.STONE_BRICKS, Blocks.AIR, Blocks.WATER, false));
  }

  @ParameterizedTest(name = "{0} under {1} under {2}: {3}")
  @MethodSource("spots")
  @DisplayName("a spot fits a resident on a solid dry floor with two open blocks above")
  void fits(final Block floor, final Block feet, final Block head, final boolean fits) {
    assertEquals(
        fits,
        StandingSpot.fits(floor.getDefaultState(), feet.getDefaultState(), head.getDefaultState()));
  }

  static Stream<Arguments> roofs() {
    return Stream.of(
        Arguments.of(List.of(), false),
        Arguments.of(List.of(Blocks.AIR, Blocks.AIR), false),
        Arguments.of(List.of(Blocks.AIR, Blocks.GLASS), false),
        Arguments.of(List.of(Blocks.OAK_LEAVES), false),
        Arguments.of(List.of(Blocks.AIR, Blocks.DARK_OAK_PLANKS), true),
        Arguments.of(List.of(Blocks.STONE_BRICKS, Blocks.AIR), true));
  }

  @ParameterizedTest(name = "{0} above: {1}")
  @MethodSource("roofs")
  @DisplayName("a spot is sheltered only when a solid block above keeps the sun off it")
  void sheltered(final List<Block> above, final boolean sheltered) {
    assertEquals(
        sheltered, StandingSpot.sheltered(above.stream().map(Block::getDefaultState).toList()));
  }
}
