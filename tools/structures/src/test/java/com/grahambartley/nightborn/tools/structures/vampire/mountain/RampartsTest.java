package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RampartsTest {

  private static Template built() {
    final Template t =
        new Template(
            "vampire/test", MountainCastle.SIZE, MountainCastle.HEIGHT, MountainCastle.SIZE);
    Crag.build(t);
    Ramparts.build(t);
    return t;
  }

  @ParameterizedTest(name = "{0},{1}: wall {2}, outer face {3}, walk {4}")
  @CsvSource({
    "6, 20, true, true, false",
    "7, 20, true, false, true",
    "8, 20, false, false, false",
    "30, 4, true, true, false",
    "30, 43, true, false, true",
    "30, 44, true, true, false",
    "10, 50, true, true, false",
    "11, 50, true, false, true",
    "30, 57, true, false, true",
    "30, 50, false, false, false",
    "2, 2, false, false, false"
  })
  void twoBlockWallsRingBothWards(
      final int x, final int z, final boolean wall, final boolean face, final boolean walk) {
    assertEquals(wall, Ramparts.onWall(x, z));
    assertEquals(face, Ramparts.outerFace(x, z));
    assertEquals(walk, Ramparts.onWalk(x, z));
  }

  @ParameterizedTest(name = "the wall at {0},{1} tops out at {2}")
  @CsvSource({"6, 20, 40", "7, 20, 40", "10, 50, 34", "40, 58, 34"})
  void wallsRiseEightOverTheirWardWithBattlementsOutside(final int x, final int z, final int top) {
    final Template t = built();
    assertEquals(top, Ramparts.top(x, z));
    assertTrue(t.get(x, top, z).contains("limewashed_bricks"), t.get(x, top, z));
    assertTrue(t.get(x, Crag.level(x, z), z).contains("stone_bricks"));
    if (Ramparts.outerFace(x, z)) {
      assertTrue(t.get(x, top + 1, z).contains("limewashed_bricks"));
      assertEquals((x + z) % 2 == 0, !BlockState.AIR.equals(t.get(x, top + 2, z)));
    } else {
      assertEquals(BlockState.AIR, t.get(x, top + 1, z));
      assertEquals(BlockState.AIR, t.get(x, top + 2, z));
    }
  }

  @ParameterizedTest(name = "the inner gate is open at {0},{1},{2}")
  @CsvSource({"29, 33, 44", "31, 37, 43", "33, 33, 43", "31, 33, 44"})
  void theInnerGateArchesThroughTheWallBetweenTheWards(final int x, final int y, final int z) {
    final Template t = built();
    assertEquals(BlockState.AIR, t.get(x, y, z));
    assertTrue(t.get(29, 37, 44).contains("half=top"), t.get(29, 37, 44));
    assertTrue(t.get(31, 38, 44).contains("limewashed_bricks"));
    assertTrue(t.get(28, 34, 44).contains("limewashed_bricks"));
  }

  @ParameterizedTest(name = "step {0} of the grand stair")
  @CsvSource({"0", "1", "2", "3", "4", "5"})
  void theGrandStairClimbsFromTheLowerWardToTheGate(final int step) {
    final Template t = built();
    final int z = Ramparts.STAIR_FOOT - step;
    final int y = MountainCastle.LOW + 1 + step;
    for (int x = Ramparts.GATE_WEST; x <= Ramparts.GATE_EAST; x++) {
      assertTrue(t.get(x, y, z).contains("stone_brick_stairs[facing=north"), t.get(x, y, z));
      assertEquals(BlockState.AIR, t.get(x, y + 1, z));
    }
    assertTrue(t.get(Ramparts.GATE_WEST - 1, y + 1, z).contains("stone_brick_wall"));
    assertTrue(t.get(Ramparts.GATE_EAST + 1, y + 1, z).contains("stone_brick_wall"));
    assertEquals(MountainCastle.UP, MountainCastle.LOW + 6);
  }

  @ParameterizedTest(name = "base {0} faces z {1}")
  @CsvSource({"32, 31", "26, 51"})
  void towersFaceTheMiddleOfTheirOwnWard(final int base, final int heart) {
    assertEquals(heart, Ramparts.heart(base));
  }
}
