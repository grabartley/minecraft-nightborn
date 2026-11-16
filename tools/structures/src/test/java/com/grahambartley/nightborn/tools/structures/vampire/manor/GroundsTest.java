package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroundsTest {

  static Template grounds;

  @BeforeAll
  static void build() {
    grounds = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    Grounds.build(grounds);
  }

  @ParameterizedTest(name = "{0},{1} on the carriage sweep: {2}")
  @CsvSource({"27, 30, true", "32, 35, true", "27, 35, false", "27, 41, false", "20, 35, false"})
  void theSweepIsARing(final int x, final int z, final boolean on) {
    assertEquals(on, Grounds.onSweep(x, z));
  }

  @ParameterizedTest(name = "the gate is open at x {0}")
  @CsvSource({"26", "27", "28"})
  void theGateIsOpen(final int x) {
    assertEquals("minecraft:air", grounds.get(x, 1, Grounds.RAILING_Z));
    assertTrue(grounds.get(x, 0, Grounds.RAILING_Z).matches("minecraft:(gravel|coarse_dirt)"));
  }

  @ParameterizedTest(name = "the railing at x {0} is {1} at y 2")
  @CsvSource({"25, bricks", "29, bricks", "35, bricks", "30, iron_bars", "10, iron_bars"})
  void railingsRunBetweenPiers(final int x, final String block) {
    assertTrue(
        grounds.get(x, 2, Grounds.RAILING_Z).contains(block), grounds.get(x, 2, Grounds.RAILING_Z));
  }

  @ParameterizedTest(name = "gate pier x {0} carries a lantern")
  @CsvSource({"25", "29"})
  void gatePiersCarryLanterns(final int x) {
    assertTrue(grounds.get(x, 6, Grounds.RAILING_Z).startsWith("minecraft:lantern"));
  }

  @ParameterizedTest(name = "the ground at {0},{1} is {2}")
  @CsvSource({"1, 20, dirt_path|grass_block", "30, 5, dirt_path|grass_block", "27, 35, moss_block"})
  void pathsWindRoundTheHouse(final int x, final int z, final String blocks) {
    assertTrue(grounds.get(x, 0, z).matches("minecraft:(" + blocks + ").*"), grounds.get(x, 0, z));
  }

  @ParameterizedTest(name = "nothing is left undefined at y {0}")
  @CsvSource({"0", "10", "33"})
  void everyCellIsDefined(final int y) {
    for (int x = 0; x < Manor.SIZE_X; x++) {
      for (int z = 0; z < Manor.SIZE_Z; z++) {
        assertTrue(grounds.get(x, y, z) != null, x + "," + y + "," + z);
      }
    }
  }
}
