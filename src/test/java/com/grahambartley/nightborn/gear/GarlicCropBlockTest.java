package com.grahambartley.nightborn.gear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EmptyBlockView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class GarlicCropBlockTest {

  private static final GarlicCropBlock GARLIC = (GarlicCropBlock) GearBlocks.GARLIC;

  @Test
  @DisplayName("garlic is replanted from its own bulbs")
  void seedsAreGarlic() {
    assertEquals(GearItems.GARLIC, GARLIC.getSeedsItem().asItem());
  }

  @Test
  @DisplayName("garlic grows through eight ages like a carrot")
  void ages() {
    assertEquals(7, GARLIC.getMaxAge());
  }

  static Stream<Arguments> floors() {
    return Stream.of(
        Arguments.of(Blocks.FARMLAND, true),
        Arguments.of(Blocks.DIRT, true),
        Arguments.of(Blocks.COARSE_DIRT, true),
        Arguments.of(Blocks.GRASS_BLOCK, true),
        Arguments.of(Blocks.MUD, false),
        Arguments.of(Blocks.MOSS_BLOCK, false),
        Arguments.of(Blocks.STONE, false),
        Arguments.of(Blocks.SAND, false));
  }

  @ParameterizedTest(name = "{0}: {1}")
  @MethodSource("floors")
  @DisplayName("garlic is farmed on farmland, grows on dirt and grass, and nowhere else")
  void floor(final Block floor, final boolean plantable) {
    assertEquals(
        plantable,
        GARLIC.canPlantOnTop(floor.getDefaultState(), EmptyBlockView.INSTANCE, BlockPos.ORIGIN));
  }
}
