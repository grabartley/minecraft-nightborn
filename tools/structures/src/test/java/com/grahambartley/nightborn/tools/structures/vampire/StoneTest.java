package com.grahambartley.nightborn.tools.structures.vampire;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StoneTest {

  private static final Stone STONE =
      new Stone("stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", 30);

  @ParameterizedTest(name = "at {0}% wear about {1} of 1000 blocks weather")
  @CsvSource({"0, 0", "30, 300", "100, 1000"})
  void wearSetsHowMuchWeathers(final int wear, final int expected) {
    final Stone worn = STONE.worn(wear);
    int weathered = 0;
    for (int i = 0; i < 1000; i++) {
      if (!worn.at(i, i / 7, i / 3).equals("stone_bricks")) {
        weathered++;
      }
    }
    assertTrue(Math.abs(weathered - expected) <= 60, "weathered " + weathered);
  }

  @Test
  void theSameCellAlwaysGetsTheSameBlock() {
    assertEquals(STONE.at(4, 5, 6), STONE.at(4, 5, 6));
  }

  @Test
  void wallsLeaveTheInsideEmpty() {
    final Template t = new Template("vampire/test", 5, 2, 5);
    STONE.walls(t, 0, 0, 0, 4, 1, 4);
    assertEquals(null, t.get(2, 0, 2));
    assertTrue(t.get(0, 1, 2).contains("stone_bricks"));
    assertTrue(t.get(4, 0, 4).contains("stone_bricks"));
  }
}
