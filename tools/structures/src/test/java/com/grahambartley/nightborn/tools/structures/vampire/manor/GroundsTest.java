package com.grahambartley.nightborn.tools.structures.vampire.manor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GroundsTest {

  static Template grounds;

  @BeforeAll
  static void build() {
    grounds = new Template("vampire/test", Manor.SIZE_X, Manor.SIZE_Y, Manor.SIZE_Z);
    Grounds.build(grounds);
  }

  @ParameterizedTest(name = "{0},{1} on the sweep {2}")
  @CsvSource({"38, 52, true", "43, 57, true", "38, 57, false", "38, 63, false", "30, 57, false"})
  void theSweepIsARing(final int x, final int z, final boolean on) {
    assertEquals(on, Grounds.onSweep(x, z));
  }

  @Test
  void theSweepSitsBetweenTheStepsAndTheGate() {
    assertEquals((ManorFront.STEPS_END + 1 + Grounds.RAILING_Z) / 2, Grounds.SWEEP_Z);
    assertEquals(Manor.SIZE_Z - 2, Grounds.RAILING_Z);
  }

  @ParameterizedTest(name = "the gate is open at x {0}")
  @CsvSource({"37", "38", "39"})
  void theGateIsOpen(final int x) {
    assertEquals("minecraft:air", grounds.get(x, 1, Grounds.RAILING_Z));
    assertTrue(grounds.get(x, 0, Grounds.RAILING_Z).matches("minecraft:(gravel|coarse_dirt)"));
  }

  @ParameterizedTest(name = "the railing at x {0} is {1} at y 2")
  @CsvSource({
    "36, gothic_stone",
    "40, gothic_stone",
    "46, gothic_stone",
    "41, iron_bars",
    "11, iron_bars"
  })
  void railingsRunBetweenPiers(final int x, final String block) {
    assertTrue(
        grounds.get(x, 2, Grounds.RAILING_Z).contains(block), grounds.get(x, 2, Grounds.RAILING_Z));
  }

  @ParameterizedTest(name = "gate pier x {0} carries a lantern")
  @CsvSource({"36", "40"})
  void gatePiersCarryLanterns(final int x) {
    assertTrue(grounds.get(x, 6, Grounds.RAILING_Z).startsWith("minecraft:lantern"));
  }

  @ParameterizedTest(name = "the ground at {0},{1} is {2}")
  @CsvSource({
    "1, 30, dirt_path|grass_block",
    "30, 10, dirt_path|grass_block",
    "38, 57, moss_block",
    "38, 52, gravel|coarse_dirt"
  })
  void pathsWindRoundTheHouse(final int x, final int z, final String blocks) {
    assertTrue(grounds.get(x, 0, z).matches("minecraft:(" + blocks + ").*"), grounds.get(x, 0, z));
  }

  @ParameterizedTest(name = "back step at z {0} is at y {1}")
  @CsvSource({"15, 5", "13, 3", "11, 1"})
  void stepsLeadDownFromTheKitchenDoor(final int z, final int y) {
    assertTrue(
        grounds.get(ManorShell.EAST_THROUGH, y, z).contains("stairs[facing=south"),
        grounds.get(ManorShell.EAST_THROUGH, y, z));
  }
}
