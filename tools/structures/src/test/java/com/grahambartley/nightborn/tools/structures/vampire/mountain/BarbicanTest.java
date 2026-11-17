package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BarbicanTest {

  private static Template box() {
    return new Template(
        "vampire/test", MountainCastle.SIZE, MountainCastle.HEIGHT, MountainCastle.SIZE);
  }

  private static Template built() {
    final Template t = box();
    Crag.build(t);
    Ramparts.build(t);
    Barbican.build(t);
    return t;
  }

  @ParameterizedTest(name = "the passage is open at {0},{1},{2}")
  @CsvSource({"30, 27, 54", "31, 30, 57", "32, 27, 61", "31, 28, 60"})
  void theGateRunsStraightThrough(final int x, final int y, final int z) {
    assertEquals(BlockState.AIR, built().get(x, y, z));
  }

  @ParameterizedTest(name = "bars hang in the gate at x {0}")
  @CsvSource({"30", "31", "32"})
  void aRaisedGrilleHangsInTheOuterArch(final int x) {
    final Template t = built();
    assertTrue(t.get(x, 30, Barbican.SOUTH - 1).contains("iron_bars"));
    assertEquals(BlockState.AIR, t.get(x, 29, Barbican.SOUTH - 1));
  }

  @ParameterizedTest(name = "the gate room opens onto the wall walk at x {0}")
  @CsvSource({"26", "36"})
  void theRoomOverTheGateJoinsTheWallWalk(final int x) {
    final Template t = built();
    assertEquals(Ramparts.LOW_TOP, Barbican.ROOM);
    assertEquals(BlockState.AIR, t.get(x, Barbican.ROOM + 1, 57));
    assertEquals(BlockState.AIR, t.get(x, Barbican.ROOM + 2, 57));
    assertEquals("minecraft:dark_oak_planks", t.get(31, Barbican.ROOM, 57));
    assertTrue(t.get(31, Barbican.ROOM - 1, 57).contains("limewashed_bricks"));
  }

  @ParameterizedTest(name = "course {0} of the roof is {1}")
  @CsvSource({"0, clay_roof_tile_stairs", "1, clay_roof_tiles", "2, clay_roof_tile_stairs"})
  void aTiledGableWithSnowLedgesCapsIt(final int course, final String block) {
    final Template t = built();
    final String tile = t.get(31, Barbican.TOP + 1 + course, Barbican.NORTH - 1 + course);
    assertTrue(tile.startsWith("nightborn:" + block), tile);
  }

  @ParameterizedTest(name = "a gargoyle watches from x {0}")
  @CsvSource({"26", "36"})
  void gargoylesFlankTheGate(final int x) {
    final Template t = built();
    assertTrue(t.get(x, MountainCastle.LOW + 4, Barbican.SOUTH + 1).contains("gargoyle"));
    assertTrue(t.get(x, MountainCastle.LOW + 3, Barbican.SOUTH + 1).contains("half=top"));
  }
}
