package com.grahambartley.nightborn.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.grahambartley.nightborn.MinecraftBootstrapExtension;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PointedDripstoneBlock;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(MinecraftBootstrapExtension.class)
class CavePathTest {

  static Stream<Arguments> blocks() {
    return Stream.of(
        Arguments.of(Blocks.DEEPSLATE.getDefaultState(), false, true),
        Arguments.of(Blocks.TUFF.getDefaultState(), false, true),
        Arguments.of(Blocks.CAVE_AIR.getDefaultState(), true, false),
        Arguments.of(Blocks.AIR.getDefaultState(), true, false),
        Arguments.of(Blocks.GLOW_LICHEN.getDefaultState(), true, false),
        Arguments.of(Blocks.WATER.getDefaultState(), true, true),
        Arguments.of(Blocks.LAVA.getDefaultState(), true, true),
        Arguments.of(
            Blocks.POINTED_DRIPSTONE
                .getDefaultState()
                .with(PointedDripstoneBlock.WATERLOGGED, false),
            false,
            true));
  }

  @ParameterizedTest(name = "{0}: needs a floor {1}, needs clearing {2}")
  @MethodSource("blocks")
  void laysAFloorUnderOpenGroundAndClearsWhatBlocksTheWay(
      final BlockState state, final boolean floor, final boolean clear) {
    assertEquals(floor, CavePath.needsFloor(state));
    assertEquals(clear, CavePath.needsClearing(state));
  }

  static Stream<Arguments> routes() {
    return Stream.of(
        Arguments.of(List.of(new BlockPos(3, -30, 7)), new BlockBox(3, -30, 7, 3, -28, 7)),
        Arguments.of(
            List.of(new BlockPos(0, -30, 0), new BlockPos(0, -30, 1), new BlockPos(1, -29, 1)),
            new BlockBox(0, -30, 0, 1, -27, 1)),
        Arguments.of(
            List.of(new BlockPos(-5, -12, 4), new BlockPos(-6, -13, 4)),
            new BlockBox(-6, -13, 4, -5, -10, 4)));
  }

  @ParameterizedTest(name = "a route over {0} takes the box {1}")
  @MethodSource("routes")
  void coversEveryFloorAndTheHeadroomAboveIt(final List<BlockPos> floors, final BlockBox box) {
    assertEquals(
        box,
        new CavePath(
                floors,
                Blocks.COBBLED_DEEPSLATE.getDefaultState(),
                CaveRoom.PERSON,
                Direction.NORTH)
            .getBoundingBox());
  }

  @Test
  void refusesAnEmptyRoute() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new CavePath(
                List.of(),
                Blocks.COBBLED_DEEPSLATE.getDefaultState(),
                CaveRoom.PERSON,
                Direction.EAST));
  }
}
