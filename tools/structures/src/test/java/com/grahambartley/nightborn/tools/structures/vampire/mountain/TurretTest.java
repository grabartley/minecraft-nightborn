package com.grahambartley.nightborn.tools.structures.vampire.mountain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.nightborn.tools.structures.template.BlockState;
import com.grahambartley.nightborn.tools.structures.template.Pos;
import com.grahambartley.nightborn.tools.structures.template.Template;
import java.util.List;
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
  void standsOnTheRockWithRoomsAndASpire(final boolean round) {
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
    assertTrue(t.cells().values().stream().noneMatch(state -> state.contains("ladder")));
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

  @ParameterizedTest(name = "a turret of radius {0} winds its stair round {1} cells")
  @CsvSource({"3, 8", "4, 16", "5, 24"})
  void theStairLoopKeepsOffTheWallAndTheMiddle(final int radius, final int cells) {
    final Turret turret = new Turret(20, 20, radius, 0, 12, true, 6);
    final List<Pos> loop = turret.loop();
    assertEquals(cells, loop.size());
    assertEquals(cells, loop.stream().distinct().count());
    for (int i = 0; i < loop.size(); i++) {
      final Pos at = loop.get(i);
      final Pos next = loop.get((i + 1) % loop.size());
      assertTrue(turret.inside(at.x(), at.z(), radius - 1), "inside at " + at);
      assertTrue(!turret.inside(at.x(), at.z(), radius - 3), "off the middle at " + at);
      assertEquals(1, Math.abs(at.x() - next.x()) + Math.abs(at.z() - next.z()), "from " + at);
    }
  }

  @ParameterizedTest(name = "turret {0} climbs to its landing at y {1}")
  @CsvSource({"0, 46", "1, 40", "2, 40", "3, 40", "4, 34", "5, 34"})
  void aSpiralStairClimbsFromTheWardToTheHighestFloor(final int index, final int landing) {
    final Template t = box();
    Crag.build(t);
    Ramparts.build(t);
    final Turret turret = MountainCastle.turrets(MountainCastle.EYRIE).get(index);
    turret.build(t);
    assertEquals(landing, turret.landing());
    final List<Pos> loop = turret.loop();
    for (int y = turret.base() + 1; y <= landing; y++) {
      final Pos at = loop.get(y - turret.base() - 1);
      assertTrue(
          t.get(at.x(), y, at.z()).contains(MountainCastle.KEEP_STEP + "[facing="),
          "step at " + at + " y " + y);
      assertEquals(BlockState.AIR, t.get(at.x(), y + 1, at.z()));
      assertEquals(BlockState.AIR, t.get(at.x(), y + 2, at.z()));
    }
  }

  @ParameterizedTest(name = "{0} is solid wall: {1}")
  @CsvSource({
    "nightborn:limewashed_bricks, true",
    "minecraft:air, false",
    "minecraft:glass_pane, false",
    "nightborn:gothic_stained_glass_pane, false",
    "minecraft:iron_bars, false"
  })
  void onlyMasonryCountsAsSolidWall(final String state, final boolean solid) {
    assertEquals(solid, Turret.solid(state));
  }
}
