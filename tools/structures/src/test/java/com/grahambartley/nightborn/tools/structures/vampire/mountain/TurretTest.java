package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Template;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TurretTest {

  private static Template box() {
    return new Template(
        "vampire/test", MountainCastle.SIZE, MountainCastle.HEIGHT, MountainCastle.SIZE);
  }

  private static Turret northWest(final boolean round) {
    return new Turret(9, 7, 5, MountainCastle.UP, Ramparts.TOP + 12, round, Ramparts.TOP);
  }

  @ParameterizedTest(name = "round {0}: {1},{2} is wall {3}")
  @CsvSource({
    "true, 14, 7, true",
    "true, 13, 7, false",
    "true, 13, 11, false",
    "true, 12, 11, true",
    "false, 14, 12, true",
    "false, 13, 11, false",
    "false, 15, 7, false"
  })
  void wallsRingItsFootprint(final boolean round, final int x, final int z, final boolean wall) {
    assertEquals(wall, northWest(round).wall(x, z));
  }

  @ParameterizedTest(name = "y {0} is a floor: {1}")
  @CsvSource({"32, false", "38, false", "40, true", "46, true", "52, false", "45, false"})
  void floorsLineUpWithTheWallWalk(final int y, final boolean floor) {
    assertEquals(floor, northWest(true).floor(y));
  }

  @ParameterizedTest(name = "round {0}")
  @CsvSource({"true", "false"})
  void standsOnTheRockWithRoomsALadderAndASpire(final boolean round) {
    final Template t = box();
    final Turret turret = northWest(round);
    turret.build(t);
    assertEquals("minecraft:" + Crag.rock(9, 0, 7), t.get(9, 0, 7));
    assertEquals(BlockState.AIR, t.get(9, MountainCastle.UP + 1, 7));
    assertEquals("minecraft:dark_oak_planks", t.get(9, Ramparts.TOP, 7));
    assertEquals("minecraft:dark_oak_planks", t.get(9, turret.top(), 7));
    assertTrue(t.get(14, MountainCastle.UP + 5, 7).startsWith("nightborn:"), "wall");
    assertTrue(t.get(4, MountainCastle.UP + 3, 7).startsWith("minecraft:glass_pane"));
    assertTrue(t.get(4, Ramparts.TOP + 3, 7).startsWith("minecraft:glass_pane"));
    for (int y = MountainCastle.UP + 1; y < turret.top(); y++) {
      assertTrue(t.get(5, y, 6).startsWith("minecraft:ladder"), "ladder at " + y);
    }
    assertTrue(t.get(9, turret.roofTop(), 7).startsWith("minecraft:lightning_rod"));
  }

  @ParameterizedTest(name = "round {0}")
  @CsvSource({"true", "false"})
  void opensOntoItsWardAndOntoTheWallWalk(final boolean round) {
    final Template t = box();
    northWest(round).build(t);
    assertEquals(BlockState.AIR, t.get(14, MountainCastle.UP + 1, 7));
    assertEquals(BlockState.AIR, t.get(14, MountainCastle.UP + 2, 7));
    assertTrue(!BlockState.AIR.equals(t.get(14, MountainCastle.UP + 3, 7)));
    assertEquals(BlockState.AIR, t.get(14, Ramparts.TOP + 1, 5));
    assertEquals(BlockState.AIR, t.get(7, Ramparts.TOP + 2, 12));
  }
}
