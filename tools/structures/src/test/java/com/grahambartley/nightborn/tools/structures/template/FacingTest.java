package com.grahambartley.nightborn.tools.structures.template;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class FacingTest {

  @ParameterizedTest
  @CsvSource({
    "north, 0, 0, -1",
    "south, 0, 0, 1",
    "east, 1, 0, 0",
    "west, -1, 0, 0",
    "up, 0, 1, 0",
    "down, 0, -1, 0"
  })
  void stepsOneBlockTowardsEachFacing(final String facing, final int x, final int y, final int z) {
    assertEquals(new Pos(x, y, z), Facing.STEP.get(facing));
  }

  @Test
  void listsTheFourSidesFirst() {
    assertEquals(
        List.of("north", "south", "east", "west"), List.copyOf(Facing.STEP.keySet()).subList(0, 4));
  }

  @ParameterizedTest
  @CsvSource({"north, south, east", "east, west, south", "south, north, west", "west, east, north"})
  void opposesAndTurnsClockwise(
      final String facing, final String opposite, final String clockwise) {
    assertEquals(opposite, Facing.OPPOSITE.get(facing));
    assertEquals(clockwise, Facing.CLOCKWISE.get(facing));
  }
}
