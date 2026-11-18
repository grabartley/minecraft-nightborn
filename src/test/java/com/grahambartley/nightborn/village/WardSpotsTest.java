package com.grahambartley.nightborn.village;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class WardSpotsTest {

  private static final BlockPos DOOR = new BlockPos(10, 64, 10);

  @ParameterizedTest(name = "door facing {0}")
  @EnumSource(
      value = Direction.class,
      names = {"NORTH", "SOUTH", "EAST", "WEST"})
  @DisplayName("garlic hangs at head height on the wall either side of a door, facing out from it")
  void besideDoor(final Direction facing) {
    final List<WardSpots.Spot> spots = WardSpots.besideDoor(DOOR, facing);
    assertEquals(4, spots.size());
    for (final WardSpots.Spot spot : spots) {
      final BlockPos wall = spot.pos().offset(spot.facing().getOpposite());
      assertEquals(DOOR.getY() + 1, spot.pos().getY());
      assertEquals(1, wall.getManhattanDistance(DOOR.up()));
      assertTrue(spot.facing().getAxis() == facing.getAxis());
    }
  }

  @ParameterizedTest(name = "bell at {0}, 70, {1}")
  @CsvSource({"0, 0", "-35, 120"})
  @DisplayName("the crucifix is tried nearest the bell first, never in the bell's own block")
  void aroundBell(final int x, final int z) {
    final BlockPos bell = new BlockPos(x, 70, z);
    final List<WardSpots.Spot> spots = WardSpots.aroundBell(bell);
    assertEquals(1.0, spots.get(0).pos().getSquaredDistance(bell));
    assertTrue(spots.stream().noneMatch(spot -> spot.pos().equals(bell)));
    for (int i = 1; i < spots.size(); i++) {
      assertTrue(
          spots.get(i - 1).pos().getSquaredDistance(bell)
              <= spots.get(i).pos().getSquaredDistance(bell));
    }
  }
}
