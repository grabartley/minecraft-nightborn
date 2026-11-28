package com.grahambartley.nightborn.decor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WallMountTest {

  private static List<Direction> directions(final String names) {
    return names.isEmpty()
        ? List.of()
        : Arrays.stream(names.split(" ")).map(Direction::valueOf).toList();
  }

  @ParameterizedTest(name = "looking {0} tries facing {1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "NORTH DOWN EAST UP WEST SOUTH | SOUTH WEST EAST NORTH",
        "DOWN UP | ''",
        "UP WEST | EAST",
        "EAST | WEST",
        "'' | ''"
      })
  @DisplayName("a wall piece faces away from each wall the player looks at, nearest first")
  void facings(final String looked, final String tried) {
    assertEquals(
        directions(tried), WallMount.facings(directions(looked).toArray(Direction[]::new)));
  }
}
