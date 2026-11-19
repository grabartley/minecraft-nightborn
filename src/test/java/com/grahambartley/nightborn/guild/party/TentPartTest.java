package com.grahambartley.nightborn.guild.party;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TentPartTest {

  private static final BlockPos SOMEWHERE = new BlockPos(103, 64, -27);

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("every part finds its way back to the roof's middle")
  void everyPartResolvesToTheOrigin(final Direction facing) {
    for (final TentPart part : TentPart.values()) {
      final BlockPos at = SOMEWHERE.add(part.offset(facing));
      assertEquals(SOMEWHERE, part.origin(at, facing), part.asString());
    }
  }

  @ParameterizedTest(name = "facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("the parts are ten distinct cells over a three by three by two footprint")
  void partsAreDistinctCells(final Direction facing) {
    final Set<BlockPos> cells =
        Arrays.stream(TentPart.values())
            .map(part -> part.offset(facing))
            .collect(Collectors.toSet());
    assertEquals(TentPart.values().length, cells.size());
    assertEquals(3, cells.stream().map(BlockPos::getX).distinct().count());
    assertEquals(3, cells.stream().map(BlockPos::getZ).distinct().count());
    assertEquals(2, cells.stream().map(BlockPos::getY).distinct().count());
  }

  @ParameterizedTest(name = "facing {0}, {1} lands at {2},{3},{4}")
  @CsvSource({
    "NORTH, RIGHT_FRONT, 1, -1, -1",
    "NORTH, BACK, 0, -1, 1",
    "SOUTH, RIGHT_FRONT, -1, -1, 1",
    "EAST, LEFT_BACK, -1, -1, -1",
    "WEST, ROOF_FRONT, -1, 0, 0"
  })
  @DisplayName("right is the clockwise turn from the open front and back is behind it")
  void partsTurnWithTheTent(
      final Direction facing, final TentPart part, final int x, final int y, final int z) {
    assertEquals(new BlockPos(x, y, z), part.offset(facing));
  }
}
